package com.transport.erp.vehicle.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VehicleResponse {

    private UUID id;
    private String registrationNumber;
    private String vehicleType;
    private String make;
    private String model;
    private Integer year;
    private String fuelType;
    private Integer capacity;
    private String status;
    private BigDecimal currentOdometer;
    private String chassisNumber;
    private String engineNumber;
    private String gpsDeviceId;
    private LocalDate insuranceExpiry;
    private LocalDate fitnessExpiry;
    private LocalDate permitExpiry;
    private LocalDate pollutionExpiry;
    private LocalDate taxExpiry;
    private UUID branchId;
    private String branchName;
    private Instant createdAt;
}
