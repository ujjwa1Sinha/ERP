package com.transport.erp.fuel.service;

import com.transport.erp.common.exception.ResourceNotFoundException;
import com.transport.erp.driver.domain.Driver;
import com.transport.erp.driver.repository.DriverRepository;
import com.transport.erp.fuel.domain.FuelTransaction;
import com.transport.erp.fuel.dto.FuelTransactionRequest;
import com.transport.erp.fuel.dto.FuelTransactionResponse;
import com.transport.erp.fuel.repository.FuelTransactionRepository;
import com.transport.erp.trip.domain.Trip;
import com.transport.erp.trip.repository.TripRepository;
import com.transport.erp.vehicle.domain.Vehicle;
import com.transport.erp.vehicle.repository.VehicleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class FuelService {

    private final FuelTransactionRepository fuelRepository;
    private final VehicleRepository vehicleRepository;
    private final DriverRepository driverRepository;
    private final TripRepository tripRepository;

    @Transactional
    public FuelTransactionResponse createFuelTransaction(FuelTransactionRequest request) {
        Vehicle vehicle = vehicleRepository.findById(request.getVehicleId())
                .orElseThrow(() -> new ResourceNotFoundException("Vehicle", "id", request.getVehicleId()));

        Driver driver = null;
        if (request.getDriverId() != null) {
            driver = driverRepository.findById(request.getDriverId())
                    .orElseThrow(() -> new ResourceNotFoundException("Driver", "id", request.getDriverId()));
        }

        Trip trip = null;
        if (request.getTripId() != null) {
            trip = tripRepository.findById(request.getTripId())
                    .orElseThrow(() -> new ResourceNotFoundException("Trip", "id", request.getTripId()));
        }

        FuelTransaction transaction = FuelTransaction.builder()
                .vehicle(vehicle)
                .driver(driver)
                .trip(trip)
                .date(request.getDate())
                .litres(request.getLitres())
                .pricePerLitre(request.getPricePerLitre())
                .totalAmount(request.getTotalAmount())
                .odometerReading(request.getOdometerReading())
                .location(request.getLocation())
                .fuelStation(request.getFuelStation())
                .remarks(request.getRemarks())
                .build();

        // Update Vehicle's current odometer if the fuel reading is strictly higher
        // (As requested, we update historical max, but we don't rigidly constrain
        // inputs)
        if (request.getOdometerReading() != null) {
            if (vehicle.getCurrentOdometer() == null
                    || request.getOdometerReading().compareTo(vehicle.getCurrentOdometer()) > 0) {
                vehicle.setCurrentOdometer(request.getOdometerReading());
                vehicleRepository.save(vehicle);
            }
        }

        return mapToResponse(fuelRepository.save(transaction));
    }

    @Transactional(readOnly = true)
    public Page<FuelTransactionResponse> getAllFuelTransactions(Pageable pageable) {
        return fuelRepository.findAll(pageable).map(this::mapToResponse);
    }

    @Transactional(readOnly = true)
    public Page<FuelTransactionResponse> getFuelTransactionsByVehicle(UUID vehicleId, Pageable pageable) {
        return fuelRepository.findByVehicleId(vehicleId, pageable).map(this::mapToResponse);
    }

    @Transactional(readOnly = true)
    public FuelTransactionResponse getFuelTransactionById(UUID id) {
        FuelTransaction transaction = fuelRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("FuelTransaction", "id", id));
        return mapToResponse(transaction);
    }

    @Transactional
    public void deleteFuelTransaction(UUID id) {
        if (!fuelRepository.existsById(id)) {
            throw new ResourceNotFoundException("FuelTransaction", "id", id);
        }
        fuelRepository.deleteById(id);
    }

    private FuelTransactionResponse mapToResponse(FuelTransaction t) {
        return FuelTransactionResponse.builder()
                .id(t.getId())
                .vehicleId(t.getVehicle().getId())
                .vehicleRegistrationNumber(t.getVehicle().getRegistrationNumber())
                .driverId(t.getDriver() != null ? t.getDriver().getId() : null)
                .driverName(t.getDriver() != null ? t.getDriver().getName() : null)
                .tripId(t.getTrip() != null ? t.getTrip().getId() : null)
                .tripRoute(
                        t.getTrip() != null
                                ? t.getTrip().getTripNumber() + " (" + t.getTrip().getSource() + " - "
                                        + t.getTrip().getDestination() + ")"
                                : null)
                .date(t.getDate())
                .litres(t.getLitres())
                .pricePerLitre(t.getPricePerLitre())
                .totalAmount(t.getTotalAmount())
                .odometerReading(t.getOdometerReading())
                .location(t.getLocation())
                .fuelStation(t.getFuelStation())
                .remarks(t.getRemarks())
                .createdAt(t.getCreatedAt())
                .build();
    }
}
