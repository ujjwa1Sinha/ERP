package com.transport.erp.vehicle.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class VehicleRequest {

    @NotBlank(message = "Registration number is required")
    private String registrationNumber;

    @NotNull(message = "Vehicle type is required")
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
    private String insuranceFileUrl;
    private LocalDate fitnessExpiry;
    private LocalDate permitExpiry;
    private LocalDate pollutionExpiry;
    private LocalDate taxExpiry;
    private UUID branchId;
}
