package com.transport.erp.driver.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.transport.erp.common.dto.ApiResponse;
import com.transport.erp.common.dto.PagedResponse;
import com.transport.erp.driver.dto.DriverRequest;
import com.transport.erp.driver.dto.DriverResponse;
import com.transport.erp.driver.service.DriverService;
import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/drivers")
@RequiredArgsConstructor
public class DriverController {

    private final DriverService driverService;
    private final ObjectMapper objectMapper;
    private final com.transport.erp.common.service.SupabaseStorageService storageService;

    private static final long MAX_LICENSE_SIZE = 1_048_576; // 1MB
    private static final List<String> ALLOWED_TYPES = List.of(
            "application/pdf", "image/jpeg", "image/jpg", "image/png");

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAuthority('DRIVER_EDIT')")
    public ResponseEntity<ApiResponse<DriverResponse>> createDriver(
            @RequestPart("driver") String driverJson,
            @RequestPart("licenseFile") MultipartFile licenseFile) throws IOException {

        DriverRequest request = objectMapper.readValue(driverJson, DriverRequest.class);
        String fileUrl = validateAndSaveFile(licenseFile);
        request.setLicenseFileUrl(fileUrl);

        DriverResponse response = driverService.createDriver(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Driver created successfully", response));
    }

    @GetMapping
    @PreAuthorize("hasAuthority('DRIVER_VIEW')")
    public ResponseEntity<ApiResponse<PagedResponse<DriverResponse>>> getAllDrivers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "name") String sortBy) {
        PagedResponse<DriverResponse> response = driverService.getAllDrivers(page, size, sortBy);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('DRIVER_VIEW')")
    public ResponseEntity<ApiResponse<DriverResponse>> getDriverById(@PathVariable UUID id) {
        DriverResponse response = driverService.getDriverById(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/code/{employeeCode}")
    @PreAuthorize("hasAuthority('DRIVER_VIEW')")
    public ResponseEntity<ApiResponse<DriverResponse>> getDriverByCode(@PathVariable String employeeCode) {
        DriverResponse response = driverService.getDriverByEmployeeCode(employeeCode);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/branch/{branchId}")
    @PreAuthorize("hasAuthority('DRIVER_VIEW')")
    public ResponseEntity<ApiResponse<List<DriverResponse>>> getDriversByBranch(@PathVariable UUID branchId) {
        List<DriverResponse> response = driverService.getDriversByBranch(branchId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/search")
    @PreAuthorize("hasAuthority('DRIVER_VIEW')")
    public ResponseEntity<ApiResponse<PagedResponse<DriverResponse>>> searchDrivers(
            @RequestParam String name,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        PagedResponse<DriverResponse> response = driverService.searchDrivers(name, page, size);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAuthority('DRIVER_EDIT')")
    public ResponseEntity<ApiResponse<DriverResponse>> updateDriver(
            @PathVariable UUID id,
            @RequestPart("driver") String driverJson,
            @RequestPart(value = "licenseFile", required = false) MultipartFile licenseFile) throws IOException {

        DriverRequest request = objectMapper.readValue(driverJson, DriverRequest.class);

        if (licenseFile != null && !licenseFile.isEmpty()) {
            String fileUrl = validateAndSaveFile(licenseFile);
            request.setLicenseFileUrl(fileUrl);
        }

        DriverResponse response = driverService.updateDriver(id, request);
        return ResponseEntity.ok(ApiResponse.success("Driver updated successfully", response));
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAuthority('DRIVER_EDIT')")
    public ResponseEntity<ApiResponse<Void>> updateDriverStatus(
            @PathVariable UUID id, @RequestBody Map<String, String> request) {
        driverService.updateDriverStatus(id, request.get("status"));
        return ResponseEntity.ok(ApiResponse.success("Driver status updated", null));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('DRIVER_EDIT')")
    public ResponseEntity<ApiResponse<Void>> deleteDriver(@PathVariable UUID id) {
        driverService.deleteDriver(id);
        return ResponseEntity.ok(ApiResponse.success("Driver deleted successfully", null));
    }

    private String validateAndSaveFile(MultipartFile file) {
        if (file.getSize() > MAX_LICENSE_SIZE) {
            throw new IllegalArgumentException("License file exceeds 1MB limit. Size: "
                    + (file.getSize() / 1024) + "KB");
        }

        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_TYPES.contains(contentType.toLowerCase())) {
            throw new IllegalArgumentException(
                    "Invalid file type. Only PDF and JPEG/PNG files are allowed.");
        }

        // Upload to Supabase 'documents' bucket under the 'drivers' folder (or directly
        // in bucket depending on config)
        return storageService.uploadFile(file, "drivers");
    }
}
