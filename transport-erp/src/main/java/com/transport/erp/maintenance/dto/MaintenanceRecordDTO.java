package com.transport.erp.maintenance.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MaintenanceRecordDTO {
    private UUID id;
    private UUID vehicleId;
    private String vehicleRegistrationNumber;
    private String maintenanceType;
    private Instant serviceDate;
    private BigDecimal odometerReading;
    private String vendor;
    private BigDecimal cost;
    private String description;
    private Instant nextServiceDate;
    private BigDecimal nextServiceOdometer;
}
