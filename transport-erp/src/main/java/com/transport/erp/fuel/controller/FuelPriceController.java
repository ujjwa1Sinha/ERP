package com.transport.erp.fuel.controller;

import com.transport.erp.common.dto.ApiResponse;
import com.transport.erp.fuel.service.FuelPriceService;
import com.transport.erp.vehicle.domain.Vehicle;
import com.transport.erp.vehicle.repository.VehicleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/fuel/price-estimate")
@RequiredArgsConstructor
public class FuelPriceController {

    private final FuelPriceService fuelPriceService;
    private final VehicleRepository vehicleRepository;

    @GetMapping
    @PreAuthorize("hasAuthority('FUEL_VIEW') or hasAuthority('TRIP_VIEW')")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getEstimate(
            @RequestParam String source,
            @RequestParam String destination,
            @RequestParam UUID vehicleId) {

        Vehicle vehicle = vehicleRepository.findById(vehicleId)
                .orElseThrow(() -> new IllegalArgumentException("Vehicle not found"));

        String fuelType = vehicle.getFuelType() != null ? vehicle.getFuelType().name() : "DIESEL";

        BigDecimal price = fuelPriceService.getAverageTripFuelPrice(source, destination, fuelType);

        Map<String, Object> result = Map.of(
                "fuelType", fuelType != null ? fuelType : "DIESEL",
                "estimatedPricePerLitre", price,
                "source", source,
                "destination", destination);
        return ResponseEntity.ok(ApiResponse.success("Fuel price estimated", result));
    }
}
