package com.transport.erp.assignment.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AssignmentResponse {

    private UUID id;
    private UUID driverId;
    private String driverName;
    private String driverEmployeeCode;
    private UUID vehicleId;
    private String vehicleRegistrationNumber;
    private UUID tripId;
    private Instant assignedAt;
    private Instant releasedAt;
    private String role;
    private String remarks;
    private boolean active;
}
