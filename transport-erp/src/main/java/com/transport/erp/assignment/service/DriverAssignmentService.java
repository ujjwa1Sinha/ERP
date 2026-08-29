package com.transport.erp.assignment.service;

import com.transport.erp.assignment.domain.AssignmentRole;
import com.transport.erp.assignment.domain.DriverAssignment;
import com.transport.erp.assignment.dto.AssignmentRequest;
import com.transport.erp.assignment.dto.AssignmentResponse;
import com.transport.erp.assignment.repository.DriverAssignmentRepository;
import com.transport.erp.common.exception.ResourceNotFoundException;
import com.transport.erp.driver.domain.Driver;
import com.transport.erp.driver.repository.DriverRepository;
import com.transport.erp.vehicle.domain.Vehicle;
import com.transport.erp.vehicle.repository.VehicleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DriverAssignmentService {

    private final DriverAssignmentRepository assignmentRepository;
    private final DriverRepository driverRepository;
    private final VehicleRepository vehicleRepository;

    @Transactional
    public AssignmentResponse assignDriverToVehicle(AssignmentRequest request) {
        Driver driver = driverRepository.findById(request.getDriverId())
                .orElseThrow(() -> new ResourceNotFoundException("Driver", "id", request.getDriverId()));

        Vehicle vehicle = vehicleRepository.findById(request.getVehicleId())
                .orElseThrow(() -> new ResourceNotFoundException("Vehicle", "id", request.getVehicleId()));

        // Check if driver already has an active assignment
        assignmentRepository.findActiveAssignmentByDriver(driver.getId())
                .ifPresent(existing -> {
                    throw new IllegalArgumentException(
                            "Driver " + driver.getName() + " is already assigned to vehicle "
                                    + existing.getVehicle().getRegistrationNumber());
                });

        DriverAssignment assignment = DriverAssignment.builder()
                .driver(driver)
                .vehicle(vehicle)
                .tripId(request.getTripId())
                .assignedAt(Instant.now())
                .role(request.getRole() != null
                        ? AssignmentRole.valueOf(request.getRole().toUpperCase())
                        : AssignmentRole.PRIMARY_DRIVER)
                .remarks(request.getRemarks())
                .build();

        return mapToResponse(assignmentRepository.save(assignment));
    }

    @Transactional
    public AssignmentResponse releaseAssignment(UUID assignmentId) {
        DriverAssignment assignment = assignmentRepository.findById(assignmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Assignment", "id", assignmentId));

        if (assignment.getReleasedAt() != null) {
            throw new IllegalArgumentException("Assignment is already released");
        }

        assignment.setReleasedAt(Instant.now());
        return mapToResponse(assignmentRepository.save(assignment));
    }

    @Transactional(readOnly = true)
    public List<AssignmentResponse> getActiveAssignments() {
        return assignmentRepository.findAllActiveAssignments().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public AssignmentResponse getActiveAssignmentByVehicle(UUID vehicleId) {
        DriverAssignment assignment = assignmentRepository.findActiveAssignmentByVehicle(vehicleId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "No active assignment found for vehicle: " + vehicleId));
        return mapToResponse(assignment);
    }

    @Transactional(readOnly = true)
    public AssignmentResponse getActiveAssignmentByDriver(UUID driverId) {
        DriverAssignment assignment = assignmentRepository.findActiveAssignmentByDriver(driverId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "No active assignment found for driver: " + driverId));
        return mapToResponse(assignment);
    }

    @Transactional(readOnly = true)
    public List<AssignmentResponse> getAssignmentHistoryByVehicle(UUID vehicleId) {
        return assignmentRepository.findByVehicleId(vehicleId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<AssignmentResponse> getAssignmentHistoryByDriver(UUID driverId) {
        return assignmentRepository.findByDriverId(driverId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    private AssignmentResponse mapToResponse(DriverAssignment assignment) {
        return AssignmentResponse.builder()
                .id(assignment.getId())
                .driverId(assignment.getDriver().getId())
                .driverName(assignment.getDriver().getName())
                .driverEmployeeCode(assignment.getDriver().getEmployeeCode())
                .vehicleId(assignment.getVehicle().getId())
                .vehicleRegistrationNumber(assignment.getVehicle().getRegistrationNumber())
                .tripId(assignment.getTripId())
                .assignedAt(assignment.getAssignedAt())
                .releasedAt(assignment.getReleasedAt())
                .role(assignment.getRole().name())
                .remarks(assignment.getRemarks())
                .active(assignment.getReleasedAt() == null)
                .build();
    }
}
