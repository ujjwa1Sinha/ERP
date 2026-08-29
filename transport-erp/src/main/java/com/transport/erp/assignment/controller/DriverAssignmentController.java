package com.transport.erp.assignment.controller;

import com.transport.erp.assignment.dto.AssignmentRequest;
import com.transport.erp.assignment.dto.AssignmentResponse;
import com.transport.erp.assignment.service.DriverAssignmentService;
import com.transport.erp.common.dto.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/assignments")
@RequiredArgsConstructor
public class DriverAssignmentController {

    private final DriverAssignmentService assignmentService;

    @PostMapping
    @PreAuthorize("hasAuthority('ASSIGNMENT_EDIT')")
    public ResponseEntity<ApiResponse<AssignmentResponse>> assignDriver(
            @Valid @RequestBody AssignmentRequest request) {
        AssignmentResponse response = assignmentService.assignDriverToVehicle(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Driver assigned to vehicle successfully", response));
    }

    @PostMapping("/{id}/release")
    @PreAuthorize("hasAuthority('ASSIGNMENT_EDIT')")
    public ResponseEntity<ApiResponse<AssignmentResponse>> releaseAssignment(@PathVariable UUID id) {
        AssignmentResponse response = assignmentService.releaseAssignment(id);
        return ResponseEntity.ok(ApiResponse.success("Assignment released successfully", response));
    }

    @GetMapping("/active")
    @PreAuthorize("hasAuthority('ASSIGNMENT_VIEW')")
    public ResponseEntity<ApiResponse<List<AssignmentResponse>>> getActiveAssignments() {
        List<AssignmentResponse> response = assignmentService.getActiveAssignments();
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/vehicle/{vehicleId}/active")
    @PreAuthorize("hasAuthority('ASSIGNMENT_VIEW')")
    public ResponseEntity<ApiResponse<AssignmentResponse>> getActiveAssignmentByVehicle(
            @PathVariable UUID vehicleId) {
        AssignmentResponse response = assignmentService.getActiveAssignmentByVehicle(vehicleId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/driver/{driverId}/active")
    @PreAuthorize("hasAuthority('ASSIGNMENT_VIEW')")
    public ResponseEntity<ApiResponse<AssignmentResponse>> getActiveAssignmentByDriver(
            @PathVariable UUID driverId) {
        AssignmentResponse response = assignmentService.getActiveAssignmentByDriver(driverId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/vehicle/{vehicleId}/history")
    @PreAuthorize("hasAuthority('ASSIGNMENT_VIEW')")
    public ResponseEntity<ApiResponse<List<AssignmentResponse>>> getVehicleAssignmentHistory(
            @PathVariable UUID vehicleId) {
        List<AssignmentResponse> response = assignmentService.getAssignmentHistoryByVehicle(vehicleId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/driver/{driverId}/history")
    @PreAuthorize("hasAuthority('ASSIGNMENT_VIEW')")
    public ResponseEntity<ApiResponse<List<AssignmentResponse>>> getDriverAssignmentHistory(
            @PathVariable UUID driverId) {
        List<AssignmentResponse> response = assignmentService.getAssignmentHistoryByDriver(driverId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
