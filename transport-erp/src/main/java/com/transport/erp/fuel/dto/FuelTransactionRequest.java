package com.transport.erp.fuel.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
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
public class FuelTransactionRequest {

    @NotNull(message = "Vehicle ID is required")
    private UUID vehicleId;

    private UUID driverId;

    private UUID tripId;

    @NotNull(message = "Date is required")
    private Instant date;

    @NotNull(message = "Litres is required")
    @Positive(message = "Litres must be positive")
    private BigDecimal litres;

    @Positive(message = "Price per litre must be positive")
    private BigDecimal pricePerLitre;

    @NotNull(message = "Total amount is required")
    @Positive(message = "Total amount must be positive")
    private BigDecimal totalAmount;

    @Positive(message = "Odometer reading must be positive")
    private BigDecimal odometerReading;

    private String location;

    private String fuelStation;

    private String remarks;
}
