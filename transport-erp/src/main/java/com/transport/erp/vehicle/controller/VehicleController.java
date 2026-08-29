package com.transport.erp.vehicle.controller;

import com.transport.erp.common.dto.ApiResponse;
import com.transport.erp.common.dto.PagedResponse;
import com.transport.erp.vehicle.dto.VehicleRequest;
import com.transport.erp.vehicle.dto.VehicleResponse;
import com.transport.erp.vehicle.service.VehicleService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/vehicles")
@RequiredArgsConstructor
public class VehicleController {

    private final VehicleService vehicleService;

    @PostMapping
    @PreAuthorize("hasAuthority('VEHICLE_EDIT')")
    public ResponseEntity<ApiResponse<VehicleResponse>> createVehicle(@Valid @RequestBody VehicleRequest request) {
        VehicleResponse response = vehicleService.createVehicle(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Vehicle created successfully", response));
    }

    @GetMapping
    @PreAuthorize("hasAuthority('VEHICLE_VIEW')")
    public ResponseEntity<ApiResponse<PagedResponse<VehicleResponse>>> getAllVehicles(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "registrationNumber") String sortBy) {
        PagedResponse<VehicleResponse> response = vehicleService.getAllVehicles(page, size, sortBy);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('VEHICLE_VIEW')")
    public ResponseEntity<ApiResponse<VehicleResponse>> getVehicleById(@PathVariable UUID id) {
        VehicleResponse response = vehicleService.getVehicleById(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/registration/{registrationNumber}")
    @PreAuthorize("hasAuthority('VEHICLE_VIEW')")
    public ResponseEntity<ApiResponse<VehicleResponse>> getVehicleByRegistration(
            @PathVariable String registrationNumber) {
        VehicleResponse response = vehicleService.getVehicleByRegistration(registrationNumber);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/branch/{branchId}")
    @PreAuthorize("hasAuthority('VEHICLE_VIEW')")
    public ResponseEntity<ApiResponse<List<VehicleResponse>>> getVehiclesByBranch(@PathVariable UUID branchId) {
        List<VehicleResponse> response = vehicleService.getVehiclesByBranch(branchId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('VEHICLE_EDIT')")
    public ResponseEntity<ApiResponse<VehicleResponse>> updateVehicle(
            @PathVariable UUID id, @Valid @RequestBody VehicleRequest request) {
        VehicleResponse response = vehicleService.updateVehicle(id, request);
        return ResponseEntity.ok(ApiResponse.success("Vehicle updated successfully", response));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('OWNER') or hasRole('FLEET_MANAGER')")
    public ResponseEntity<ApiResponse<Void>> decommissionVehicle(@PathVariable UUID id) {
        vehicleService.decommissionVehicle(id);
        return ResponseEntity.ok(ApiResponse.success("Vehicle decommissioned successfully", null));
    }
}
