package com.transport.erp.tracking.dto;

import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.UUID;

@Data
@Builder
public class VehicleLocationResponse {
    private UUID id;
    private UUID vehicleId;
    private String vehicleRegistrationNumber;
    private UUID tripId;
    private BigDecimal latitude;
    private BigDecimal longitude;
    private BigDecimal speed;
    private BigDecimal heading;
    private BigDecimal accuracy;
    private ZonedDateTime recordedAt;
}
