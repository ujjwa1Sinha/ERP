package com.transport.erp.trip.dto;

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
public class TripRequest {

    private UUID vehicleId;
    private UUID primaryDriverId;
    private UUID secondaryDriverId;
    private UUID sourceBranchId;
    private UUID destinationBranchId;
    private String source;
    private String destination;
    private Instant plannedDeparture;
    private Instant plannedArrival;
    private String tripType;
    private BigDecimal distancePlanned;
    private String remarks;
}
