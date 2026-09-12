package com.transport.erp.tracking.service;

import com.transport.erp.tracking.domain.VehicleLocation;
import com.transport.erp.tracking.dto.VehicleLocationRequest;
import com.transport.erp.tracking.dto.VehicleLocationResponse;
import com.transport.erp.tracking.repository.VehicleLocationRepository;
import com.transport.erp.trip.domain.Trip;
import com.transport.erp.trip.domain.TripStatus;
import com.transport.erp.trip.repository.TripRepository;
import com.transport.erp.vehicle.domain.Vehicle;
import com.transport.erp.vehicle.repository.VehicleRepository;
import com.transport.erp.common.service.NotificationService;
import com.transport.erp.common.util.GeoUtils;
import com.transport.erp.common.util.PolylineUtils;
import com.transport.erp.common.service.GeocodingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class TrackingService {

    private final VehicleLocationRepository locationRepository;
    private final VehicleRepository vehicleRepository;
    private final TripRepository tripRepository;
    private final NotificationService notificationService;

    @Transactional
    public void recordLocation(VehicleLocationRequest request) {
        log.debug("Recording location for vehicleId: {}", request.getVehicleId());

        Vehicle vehicle = vehicleRepository.findById(request.getVehicleId())
                .orElseThrow(() -> new IllegalArgumentException("Vehicle not found"));

        Trip trip = null;
        if (request.getTripId() != null) {
            trip = tripRepository.findById(request.getTripId()).orElse(null);
        }

        VehicleLocation location = VehicleLocation.builder()
                .vehicle(vehicle)
                .trip(trip)
                .latitude(request.getLatitude())
                .longitude(request.getLongitude())
                .speed(request.getSpeed())
                .heading(request.getHeading())
                .accuracy(request.getAccuracy())
                .recordedAt(request.getRecordedAt() != null ? request.getRecordedAt() : ZonedDateTime.now())
                .build();

        locationRepository.save(location);

        // Phase 6 - Wandering Check
        if (request.getVehicleId() != null) {
            tripRepository
                    .findFirstByVehicleIdAndStatusOrderByActualArrivalDesc(request.getVehicleId(), TripStatus.COMPLETED)
                    .ifPresent(lastTrip -> {
                        if (lastTrip.getDestLat() != null && lastTrip.getDestLng() != null
                                && !Boolean.TRUE.equals(lastTrip.getWanderedAlertSent())) {
                            double dist = GeoUtils.haversine(request.getLatitude().doubleValue(),
                                    request.getLongitude().doubleValue(),
                                    lastTrip.getDestLat().doubleValue(), lastTrip.getDestLng().doubleValue());
                            if (dist > 15.0) {
                                notificationService.sendWanderingAlert(lastTrip, dist);
                                lastTrip.setWanderedAlertSent(true);
                                tripRepository.save(lastTrip);
                            }
                        }
                    });

            // Phase 8 - Polyline Deviation & Halt Detection on Active Trips
            if (trip != null && TripStatus.IN_TRANSIT.equals(trip.getStatus())) {
                boolean tripUpdated = false;

                // Deviation Check
                if (trip.getRoutePolyline() != null && !trip.getRoutePolyline().isEmpty()
                        && !Boolean.TRUE.equals(trip.getDeviationAlertSent())) {
                    List<GeocodingService.GeoResult> routePoints = PolylineUtils
                            .decodePolyline(trip.getRoutePolyline());
                    double proximity = PolylineUtils.minimumDistanceToRoute(request.getLatitude().doubleValue(),
                            request.getLongitude().doubleValue(), routePoints);
                    // If Off-route by > 2.0 km
                    if (proximity > 2.0) {
                        notificationService.sendDeviationAlert(trip, proximity);
                        trip.setDeviationAlertSent(true);
                        tripUpdated = true;
                    }
                }

                // Halt Detection Flow
                VehicleLocation previousLocation = locationRepository
                        .findFirstByVehicleIdOrderByRecordedAtDesc(vehicle.getId()).orElse(null);
                if (previousLocation != null) {
                    double distMoved = GeoUtils.haversine(request.getLatitude().doubleValue(),
                            request.getLongitude().doubleValue(),
                            previousLocation.getLatitude().doubleValue(),
                            previousLocation.getLongitude().doubleValue());

                    // If vehicle moved < 50 meters, it is halting
                    if (distMoved < 0.05) {
                        if (trip.getLastHaltStartTime() == null) {
                            trip.setLastHaltStartTime(
                                    request.getRecordedAt() != null ? request.getRecordedAt().toLocalDateTime()
                                            : ZonedDateTime.now().toLocalDateTime());
                            tripUpdated = true;
                        } else {
                            long haltedMinutes = ChronoUnit.MINUTES.between(trip.getLastHaltStartTime(),
                                    request.getRecordedAt() != null ? request.getRecordedAt().toLocalDateTime()
                                            : ZonedDateTime.now().toLocalDateTime());
                            // Trigger 30-min Halt alert preventing spam using lastHaltAlertTime
                            if (haltedMinutes >= 30 && (trip.getLastHaltAlertTime() == null ||
                                    ChronoUnit.MINUTES.between(trip.getLastHaltAlertTime(),
                                            ZonedDateTime.now().toLocalDateTime()) > 30)) {
                                notificationService.sendHaltAlert(trip, haltedMinutes);
                                trip.setLastHaltAlertTime(ZonedDateTime.now().toLocalDateTime());
                                tripUpdated = true;
                            }
                        }
                    } else if (trip.getLastHaltStartTime() != null) {
                        // Vehicle started moving again after a halt
                        long haltedMinutes = ChronoUnit.MINUTES.between(trip.getLastHaltStartTime(),
                                ZonedDateTime.now().toLocalDateTime());
                        trip.setLastHaltStartTime(null);

                        // Only count as a "frequent halt" if it was legitimately stopped for > 5 mins
                        if (haltedMinutes > 5) {
                            trip.setFrequentHaltsCount(
                                    (trip.getFrequentHaltsCount() != null ? trip.getFrequentHaltsCount() : 0) + 1);
                            if (trip.getFrequentHaltsCount() >= 3) {
                                notificationService.sendFrequentHaltAlert(trip, trip.getFrequentHaltsCount());
                                trip.setFrequentHaltsCount(0); // reset counter after alerting
                            }
                        }
                        tripUpdated = true;
                    }
                }

                if (tripUpdated) {
                    tripRepository.save(trip);
                }
            }
        }
    }

    @Transactional(readOnly = true)
    public VehicleLocationResponse getLatestLocation(UUID vehicleId) {
        Optional<VehicleLocation> locationOpt = locationRepository.findFirstByVehicleIdOrderByRecordedAtDesc(vehicleId);
        if (locationOpt.isEmpty()) {
            return null;
        }
        return mapToResponse(locationOpt.get());
    }

    @Transactional(readOnly = true)
    public List<VehicleLocationResponse> getAllActiveVehiclesLocations() {
        // MVP: Fetch the latest known location for ALL registered vehicles to ensure
        // seamless visibility
        List<Vehicle> allVehicles = vehicleRepository.findAll();
        List<VehicleLocationResponse> responses = new ArrayList<>();

        for (Vehicle vehicle : allVehicles) {
            locationRepository.findFirstByVehicleIdOrderByRecordedAtDesc(vehicle.getId())
                    .ifPresent(loc -> responses.add(mapToResponse(loc)));
        }
        return responses;
    }

    private VehicleLocationResponse mapToResponse(VehicleLocation location) {
        return VehicleLocationResponse.builder()
                .id(location.getId())
                .vehicleId(location.getVehicle().getId())
                .vehicleRegistrationNumber(location.getVehicle().getRegistrationNumber())
                .tripId(location.getTrip() != null ? location.getTrip().getId() : null)
                .latitude(location.getLatitude())
                .longitude(location.getLongitude())
                .speed(location.getSpeed())
                .heading(location.getHeading())
                .accuracy(location.getAccuracy())
                .recordedAt(location.getRecordedAt())
                .build();
    }
}
