package com.transport.erp.tracking.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.UUID;

@Data
public class VehicleLocationRequest {
    @NotNull(message = "Vehicle ID is required")
    private UUID vehicleId;

    private UUID tripId;

    @NotNull(message = "Latitude is required")
    private BigDecimal latitude;

    @NotNull(message = "Longitude is required")
    private BigDecimal longitude;

    private BigDecimal speed;
    private BigDecimal heading;
    private BigDecimal accuracy;
    private ZonedDateTime recordedAt;
}
