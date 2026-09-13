package com.transport.erp.trip.service;

import com.transport.erp.branch.domain.Branch;
import com.transport.erp.branch.repository.BranchRepository;
import com.transport.erp.common.dto.PagedResponse;
import com.transport.erp.driver.domain.Driver;
import com.transport.erp.driver.repository.DriverRepository;
import com.transport.erp.trip.domain.*;
import com.transport.erp.trip.dto.*;
import com.transport.erp.trip.repository.TripEventRepository;
import com.transport.erp.trip.repository.TripRepository;
import com.transport.erp.assignment.repository.DriverAssignmentRepository;
import com.transport.erp.vehicle.domain.Vehicle;
import com.transport.erp.vehicle.repository.VehicleRepository;
import com.transport.erp.common.service.GeocodingService;
import com.transport.erp.common.service.GeocodingService.GeoResult;
import com.transport.erp.common.service.RoutingService;
import com.transport.erp.common.service.NotificationService;
import com.transport.erp.common.util.GeoUtils;
import com.transport.erp.tracking.repository.VehicleLocationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.time.Instant;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class TripService {

    private final TripRepository tripRepository;
    private final TripEventRepository tripEventRepository;
    private final VehicleRepository vehicleRepository;
    private final DriverRepository driverRepository;
    private final BranchRepository branchRepository;
    private final DriverAssignmentRepository driverAssignmentRepository;
    private final GeocodingService geocodingService;
    private final RoutingService routingService;
    private final NotificationService notificationService;
    private final VehicleLocationRepository vehicleLocationRepository;
    private final StringRedisTemplate stringRedisTemplate;

    // ────────────────────────────── CREATE ──────────────────────────────

    @Transactional
    public TripResponse createTrip(TripRequest request) {
        Boolean isIdempotent = stringRedisTemplate.opsForValue().setIfAbsent("idemp:" + request.getIdempotencyKey(),
                "1", java.time.Duration.ofMinutes(10));
        if (Boolean.FALSE.equals(isIdempotent)) {
            throw new com.transport.erp.common.exception.DuplicateResourceException("Trip", "idempotencyKey",
                    request.getIdempotencyKey());
        }

        GeocodingService.GeoResult srcGeo = geocodingService.validateAndGeocode(request.getSource());
        if (srcGeo == null) {
            throw new IllegalArgumentException(
                    "Invalid source address: OSM could not resolve to a street-level location.");
        }
        GeocodingService.GeoResult destGeo = geocodingService.validateAndGeocode(request.getDestination());
        if (destGeo == null) {
            throw new IllegalArgumentException(
                    "Invalid destination address: OSM could not resolve to a street-level location.");
        }

        RoutingService.OsrmResult osrmResult = routingService.getOsrmPolyline(
                srcGeo.lat(), srcGeo.lng(),
                destGeo.lat(), destGeo.lng());
        if (osrmResult != null) {
            log.info("OSRM mapped geographic polyline established for trip ({} km)", osrmResult.distanceKm());
        }

        Trip trip = Trip.builder()
                .tripNumber(generateTripNumber())
                .source(request.getSource())
                .destination(request.getDestination())
                .plannedDeparture(request.getPlannedDeparture())
                .plannedArrival(request.getPlannedArrival())
                .tripType(request.getTripType())
                .distancePlanned(osrmResult != null ? osrmResult.distanceKm() : request.getDistancePlanned())
                .remarks(request.getRemarks())
                .status(TripStatus.PLANNED)
                .idempotencyKey(request.getIdempotencyKey())
                .sourceLat(srcGeo.lat())
                .sourceLng(srcGeo.lng())
                .destLat(destGeo.lat())
                .destLng(destGeo.lng())
                .routePolyline(osrmResult != null ? osrmResult.polyline() : null)
                .durationPlannedSeconds(osrmResult != null ? osrmResult.durationSeconds() : null)
                .build();

        // Optional FKs
        if (request.getVehicleId() != null) {
            Vehicle v = vehicleRepository.findById(request.getVehicleId())
                    .orElseThrow(() -> new NoSuchElementException("Vehicle not found"));
            trip.setVehicle(v);
        }
        if (request.getPrimaryDriverId() != null) {
            Driver d = driverRepository.findById(request.getPrimaryDriverId())
                    .orElseThrow(() -> new NoSuchElementException("Primary driver not found"));
            trip.setPrimaryDriver(d);
        }

        checkConflicts(request.getPrimaryDriverId(), request.getVehicleId(), request.getPlannedDeparture(),
                request.getPlannedArrival(), null);
        if (request.getSecondaryDriverId() != null) {
            Driver d = driverRepository.findById(request.getSecondaryDriverId())
                    .orElseThrow(() -> new NoSuchElementException("Secondary driver not found"));
            trip.setSecondaryDriver(d);
        }
        if (request.getSourceBranchId() != null) {
            Branch b = branchRepository.findById(request.getSourceBranchId())
                    .orElseThrow(() -> new NoSuchElementException("Source branch not found"));
            trip.setSourceBranch(b);
        }
        if (request.getDestinationBranchId() != null) {
            Branch b = branchRepository.findById(request.getDestinationBranchId())
                    .orElseThrow(() -> new NoSuchElementException("Destination branch not found"));
            trip.setDestinationBranch(b);
        }

        // If vehicle AND driver are pre-assigned, mark as ASSIGNED
        if (trip.getVehicle() != null && trip.getPrimaryDriver() != null) {
            trip.setStatus(TripStatus.ASSIGNED);
        }

        trip = tripRepository.save(trip);
        addEvent(trip, TripEventType.TRIP_CREATED, "Trip created: " + trip.getSource() + " → " + trip.getDestination());

        return mapToResponse(trip, true);
    }

    // ────────────────────────────── ASSIGN ──────────────────────────────

    @Transactional
    public TripResponse assignVehicle(UUID tripId, UUID vehicleId) {
        Trip trip = findTripOrThrow(tripId);
        validateStatus(trip, Set.of(TripStatus.PLANNED, TripStatus.ASSIGNED));

        Vehicle v = vehicleRepository.findById(vehicleId)
                .orElseThrow(() -> new NoSuchElementException("Vehicle not found"));

        checkConflicts(null, vehicleId, trip.getPlannedDeparture(), trip.getPlannedArrival(), tripId);

        trip.setVehicle(v);
        addEvent(trip, TripEventType.VEHICLE_ASSIGNED, "Vehicle " + v.getRegistrationNumber() + " assigned");

        if (trip.getPrimaryDriver() != null) {
            trip.setStatus(TripStatus.ASSIGNED);
        }
        return mapToResponse(tripRepository.save(trip), false);
    }

    @Transactional
    public TripResponse assignDriver(UUID tripId, UUID driverId) {
        Trip trip = findTripOrThrow(tripId);
        validateStatus(trip, Set.of(TripStatus.PLANNED, TripStatus.ASSIGNED));

        Driver d = driverRepository.findById(driverId)
                .orElseThrow(() -> new NoSuchElementException("Driver not found"));

        checkConflicts(driverId, null, trip.getPlannedDeparture(), trip.getPlannedArrival(), tripId);

        trip.setPrimaryDriver(d);
        addEvent(trip, TripEventType.DRIVER_ASSIGNED, "Driver " + d.getName() + " assigned");

        if (trip.getVehicle() != null) {
            trip.setStatus(TripStatus.ASSIGNED);
        }
        return mapToResponse(tripRepository.save(trip), false);
    }

    // ────────────────────────────── LIFECYCLE ──────────────────────────────

    @Transactional
    public TripResponse startTrip(UUID tripId) {
        Trip trip = findTripOrThrow(tripId);
        validateStatus(trip, Set.of(TripStatus.ASSIGNED));

        if (trip.getVehicle() == null || trip.getPrimaryDriver() == null) {
            throw new IllegalStateException("Vehicle and primary driver must be assigned before starting a trip");
        }

        trip.setStatus(TripStatus.STARTED);
        trip.setActualDeparture(Instant.now());
        addEvent(trip, TripEventType.TRIP_STARTED, "Trip started");

        return mapToResponse(tripRepository.save(trip), false);
    }

    @Transactional
    public TripResponse completeTrip(UUID tripId, java.math.BigDecimal distanceActual) {
        Trip trip = findTripOrThrow(tripId);
        validateStatus(trip, Set.of(TripStatus.STARTED, TripStatus.IN_TRANSIT, TripStatus.HALTED));

        trip.setStatus(TripStatus.COMPLETED);
        trip.setActualArrival(Instant.now());
        if (distanceActual != null) {
            trip.setDistanceActual(distanceActual);
        }

        if (trip.getActualDeparture() != null) {
            long actualSeconds = java.time.Duration.between(trip.getActualDeparture(), trip.getActualArrival())
                    .getSeconds();
            trip.setDurationActualSeconds(actualSeconds);
            if (trip.getDurationPlannedSeconds() != null) {
                long diff = actualSeconds - trip.getDurationPlannedSeconds();
                if (diff > 900) { // 15 mins late
                    notificationService.sendDelayAlert(trip, diff / 60);
                } else if (diff < -900) { // 15 mins early
                    notificationService.sendEarlyArrivalAlert(trip, Math.abs(diff) / 60);
                }
            }
        }

        if (trip.getVehicle() != null && trip.getDestLat() != null && trip.getDestLng() != null) {
            vehicleLocationRepository.findFirstByVehicleIdOrderByRecordedAtDesc(trip.getVehicle().getId())
                    .ifPresent(loc -> {
                        double dist = GeoUtils.haversine(loc.getLatitude(), loc.getLongitude(), trip.getDestLat(),
                                trip.getDestLng());
                        if (dist >= 5.0) {
                            notificationService.sendEarlyCompletionAlert(trip, dist);
                        }
                    });
        }

        addEvent(trip, TripEventType.TRIP_COMPLETED, "Trip completed");

        return mapToResponse(tripRepository.save(trip), false);
    }

    @Transactional
    public TripResponse cancelTrip(UUID tripId, String reason) {
        Trip trip = findTripOrThrow(tripId);
        validateStatus(trip, Set.of(TripStatus.PLANNED, TripStatus.ASSIGNED, TripStatus.STARTED, TripStatus.IN_TRANSIT,
                TripStatus.HALTED));

        trip.setStatus(TripStatus.CANCELLED);
        addEvent(trip, TripEventType.TRIP_CANCELLED, reason != null ? reason : "Trip cancelled");

        return mapToResponse(tripRepository.save(trip), false);
    }

    // ────────────────────────────── QUERIES ──────────────────────────────

    @Transactional(readOnly = true)
    public PagedResponse<TripResponse> getAllTrips(int page, int size, String sortBy, String status) {
        PageRequest pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, sortBy));
        Page<Trip> tripPage;

        if (status != null && !status.isBlank()) {
            TripStatus ts = TripStatus.valueOf(status.toUpperCase());
            tripPage = tripRepository.findByStatus(ts, pageable);
        } else {
            tripPage = tripRepository.findAll(pageable);
        }

        return buildPagedResponse(tripPage);
    }

    @Transactional(readOnly = true)
    public TripResponse getTripById(UUID id) {
        Trip trip = findTripOrThrow(id);
        return mapToResponse(trip, true);
    }

    @Transactional(readOnly = true)
    public List<TripEventResponse> getTripTimeline(UUID tripId) {
        findTripOrThrow(tripId); // verify exists
        return tripEventRepository.findByTripIdOrderByEventTimestampAsc(tripId)
                .stream()
                .map(this::mapEventToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Map<String, Long> getTripStats() {
        Map<String, Long> stats = new LinkedHashMap<>();
        for (TripStatus s : TripStatus.values()) {
            stats.put(s.name(), tripRepository.countByStatus(s));
        }
        stats.put("TOTAL", tripRepository.count());
        return stats;
    }

    // ────────────────────────────── HELPERS ──────────────────────────────

    private Trip findTripOrThrow(UUID id) {
        return tripRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Trip not found with id: " + id));
    }

    private void validateStatus(Trip trip, Set<TripStatus> allowed) {
        if (!allowed.contains(trip.getStatus())) {
            throw new IllegalStateException(
                    "Invalid status transition. Current: " + trip.getStatus() +
                            ". Allowed: " + allowed);
        }
    }

    private void checkConflicts(UUID driverId, UUID vehicleId, Instant start, Instant end, UUID excludeTripId) {
        if (start == null || end == null)
            return;

        if (driverId != null) {
            if (tripRepository.hasOverlappingTripForDriver(driverId, start, end, excludeTripId)) {
                throw new IllegalArgumentException("Driver is booked on another trip during this timeframe.");
            }
            if (driverAssignmentRepository.hasOverlappingAssignmentForDriver(driverId, start, end)) {
                throw new IllegalArgumentException("Driver has an active assignment covering this timeframe.");
            }
        }

        if (vehicleId != null) {
            if (tripRepository.hasOverlappingTripForVehicle(vehicleId, start, end, excludeTripId)) {
                throw new IllegalArgumentException("Vehicle is booked on another trip during this timeframe.");
            }
            if (driverAssignmentRepository.hasOverlappingAssignmentForVehicle(vehicleId, start, end)) {
                throw new IllegalArgumentException("Vehicle has an active assignment covering this timeframe.");
            }
        }
    }

    private void addEvent(Trip trip, TripEventType type, String remarks) {
        String username = getCurrentUsername();
        TripEvent event = TripEvent.builder()
                .trip(trip)
                .eventType(type)
                .eventTimestamp(Instant.now())
                .remarks(remarks)
                .createdBy(username)
                .createdAt(Instant.now())
                .build();
        tripEventRepository.save(event);
    }

    private String generateTripNumber() {
        String datePart = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String randomPart = String.format("%04d", new Random().nextInt(10000));
        String tripNumber = "TRP-" + datePart + "-" + randomPart;

        // Ensure uniqueness
        while (tripRepository.findByTripNumber(tripNumber).isPresent()) {
            randomPart = String.format("%04d", new Random().nextInt(10000));
            tripNumber = "TRP-" + datePart + "-" + randomPart;
        }
        return tripNumber;
    }

    private String getCurrentUsername() {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        return (auth != null && auth.isAuthenticated()) ? auth.getName() : "system";
    }

    // ────────────────────────────── MAPPERS ──────────────────────────────

    private TripResponse mapToResponse(Trip trip, boolean includeEvents) {
        TripResponse.TripResponseBuilder builder = TripResponse.builder()
                .id(trip.getId())
                .tripNumber(trip.getTripNumber())
                .source(trip.getSource())
                .destination(trip.getDestination())
                .plannedDeparture(trip.getPlannedDeparture())
                .plannedArrival(trip.getPlannedArrival())
                .actualDeparture(trip.getActualDeparture())
                .actualArrival(trip.getActualArrival())
                .status(trip.getStatus().name())
                .tripType(trip.getTripType())
                .distancePlanned(trip.getDistancePlanned())
                .distanceActual(trip.getDistanceActual())
                .durationPlannedSeconds(trip.getDurationPlannedSeconds())
                .durationActualSeconds(trip.getDurationActualSeconds())
                .remarks(trip.getRemarks())
                .createdAt(trip.getCreatedAt())
                .updatedAt(trip.getUpdatedAt())
                .createdBy(trip.getCreatedBy());

        if (trip.getVehicle() != null) {
            builder.vehicleId(trip.getVehicle().getId())
                    .vehicleRegistrationNumber(trip.getVehicle().getRegistrationNumber());
        }
        if (trip.getPrimaryDriver() != null) {
            builder.primaryDriverId(trip.getPrimaryDriver().getId())
                    .primaryDriverName(trip.getPrimaryDriver().getName());
        }
        if (trip.getSecondaryDriver() != null) {
            builder.secondaryDriverId(trip.getSecondaryDriver().getId())
                    .secondaryDriverName(trip.getSecondaryDriver().getName());
        }
        if (trip.getSourceBranch() != null) {
            builder.sourceBranchId(trip.getSourceBranch().getId())
                    .sourceBranchName(trip.getSourceBranch().getName());
        }
        if (trip.getDestinationBranch() != null) {
            builder.destinationBranchId(trip.getDestinationBranch().getId())
                    .destinationBranchName(trip.getDestinationBranch().getName());
        }

        if (includeEvents) {
            List<TripEventResponse> eventResponses = tripEventRepository
                    .findByTripIdOrderByEventTimestampAsc(trip.getId())
                    .stream()
                    .map(this::mapEventToResponse)
                    .collect(Collectors.toList());
            builder.events(eventResponses);
        }

        return builder.build();
    }

    private TripEventResponse mapEventToResponse(TripEvent event) {
        return TripEventResponse.builder()
                .id(event.getId())
                .eventType(event.getEventType().name())
                .eventTimestamp(event.getEventTimestamp())
                .latitude(event.getLatitude())
                .longitude(event.getLongitude())
                .remarks(event.getRemarks())
                .createdBy(event.getCreatedBy())
                .build();
    }

    private PagedResponse<TripResponse> buildPagedResponse(Page<Trip> page) {
        List<TripResponse> content = page.getContent().stream()
                .map(t -> mapToResponse(t, false))
                .collect(Collectors.toList());

        return PagedResponse.<TripResponse>builder()
                .content(content)
                .page(page.getNumber())
                .size(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .last(page.isLast())
                .build();
    }
}
