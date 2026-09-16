package com.transport.erp.fuel.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FuelTransactionResponse {

    private UUID id;

    private UUID vehicleId;
    private String vehicleRegistrationNumber;

    private UUID driverId;
    private String driverName;

    private UUID tripId;
    private String tripRoute;

    private Instant date;
    private BigDecimal litres;
    private BigDecimal pricePerLitre;
    private BigDecimal totalAmount;
    private BigDecimal odometerReading;
    private String location;
    private String fuelStation;
    private String remarks;

    private Instant createdAt;
}
