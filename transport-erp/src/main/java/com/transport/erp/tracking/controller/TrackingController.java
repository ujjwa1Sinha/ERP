package com.transport.erp.tracking.controller;

import com.transport.erp.common.dto.ApiResponse;
import com.transport.erp.tracking.dto.VehicleLocationRequest;
import com.transport.erp.tracking.dto.VehicleLocationResponse;
import com.transport.erp.tracking.service.TrackingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/tracking")
@RequiredArgsConstructor
public class TrackingController {

    private final TrackingService trackingService;

    @PostMapping("/location")
    // Let drivers send their own location
    public ResponseEntity<ApiResponse<Void>> recordLocation(@Valid @RequestBody VehicleLocationRequest request) {
        trackingService.recordLocation(request);
        return ResponseEntity.ok(ApiResponse.success("Location recorded", null));
    }

    @GetMapping("/active")
    @PreAuthorize("hasAuthority('TRIP_VIEW')")
    public ResponseEntity<ApiResponse<List<VehicleLocationResponse>>> getActiveVehiclesLocations() {
        List<VehicleLocationResponse> locations = trackingService.getAllActiveVehiclesLocations();
        return ResponseEntity.ok(ApiResponse.success("Fetched active vehicle locations", locations));
    }

    @GetMapping("/vehicle/{vehicleId}")
    @PreAuthorize("hasAuthority('TRIP_VIEW')")
    public ResponseEntity<ApiResponse<VehicleLocationResponse>> getLatestLocation(@PathVariable UUID vehicleId) {
        VehicleLocationResponse location = trackingService.getLatestLocation(vehicleId);
        if (location == null) {
            return ResponseEntity.ok(ApiResponse.success("No location found for vehicle", null));
        }
        return ResponseEntity.ok(ApiResponse.success("Fetched latest location", location));
    }
}
