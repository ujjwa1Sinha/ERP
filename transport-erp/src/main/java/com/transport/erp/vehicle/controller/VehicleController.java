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
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.transport.erp.common.service.SupabaseStorageService;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/vehicles")
@RequiredArgsConstructor
public class VehicleController {

    private final VehicleService vehicleService;
    private final ObjectMapper objectMapper;
    private final SupabaseStorageService storageService;

    private static final long MAX_FILE_SIZE = 1_048_576; // 1MB
    private static final List<String> ALLOWED_TYPES = List.of(
            "application/pdf", "image/jpeg", "image/jpg", "image/png");

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAuthority('VEHICLE_EDIT')")
    public ResponseEntity<ApiResponse<VehicleResponse>> createVehicle(
            @RequestPart("vehicle") String vehicleJson,
            @RequestPart(value = "insuranceFile", required = false) MultipartFile insuranceFile)
            throws java.io.IOException {

        VehicleRequest request = objectMapper.readValue(vehicleJson, VehicleRequest.class);

        if (insuranceFile != null && !insuranceFile.isEmpty()) {
            validateFile(insuranceFile);
            String fileUrl = storageService.uploadFile(insuranceFile, "vehicles/insurance");
            request.setInsuranceFileUrl(fileUrl);
        }

        VehicleResponse response = vehicleService.createVehicle(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Vehicle created successfully", response));
    }

    @GetMapping("/types")
    @PreAuthorize("hasAuthority('VEHICLE_VIEW')")
    public ResponseEntity<ApiResponse<List<String>>> getVehicleTypes() {
        return ResponseEntity.ok(ApiResponse.success(vehicleService.getAllVehicleTypes()));
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

    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAuthority('VEHICLE_EDIT')")
    public ResponseEntity<ApiResponse<VehicleResponse>> updateVehicle(
            @PathVariable UUID id,
            @RequestPart("vehicle") String vehicleJson,
            @RequestPart(value = "insuranceFile", required = false) MultipartFile insuranceFile)
            throws java.io.IOException {

        VehicleRequest request = objectMapper.readValue(vehicleJson, VehicleRequest.class);

        if (insuranceFile != null && !insuranceFile.isEmpty()) {
            validateFile(insuranceFile);
            String fileUrl = storageService.uploadFile(insuranceFile, "vehicles/insurance");
            request.setInsuranceFileUrl(fileUrl);
        }

        VehicleResponse response = vehicleService.updateVehicle(id, request);
        return ResponseEntity.ok(ApiResponse.success("Vehicle updated successfully", response));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('OWNER') or hasRole('FLEET_MANAGER')")
    public ResponseEntity<ApiResponse<Void>> decommissionVehicle(@PathVariable UUID id) {
        vehicleService.decommissionVehicle(id);
        return ResponseEntity.ok(ApiResponse.success("Vehicle decommissioned successfully", null));
    }

    private void validateFile(MultipartFile file) {
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new IllegalArgumentException("Insurance file exceeds 1MB limit. Size: "
                    + (file.getSize() / 1024) + "KB");
        }
        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_TYPES.contains(contentType.toLowerCase())) {
            throw new IllegalArgumentException(
                    "Invalid file type. Only PDF and JPEG/PNG files are allowed.");
        }
    }
}
