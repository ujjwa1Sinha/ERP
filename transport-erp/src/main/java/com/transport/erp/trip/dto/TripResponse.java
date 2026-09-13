package com.transport.erp.trip.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TripResponse {

    private UUID id;
    private String tripNumber;

    // Vehicle info
    private UUID vehicleId;
    private String vehicleRegistrationNumber;

    // Primary driver info
    private UUID primaryDriverId;
    private String primaryDriverName;

    // Secondary driver info
    private UUID secondaryDriverId;
    private String secondaryDriverName;

    // Branches
    private UUID sourceBranchId;
    private String sourceBranchName;
    private UUID destinationBranchId;
    private String destinationBranchName;

    // Locations
    private String source;
    private String destination;

    // Timestamps
    private Instant plannedDeparture;
    private Instant plannedArrival;
    private Instant actualDeparture;
    private Instant actualArrival;

    // Status & type
    private String status;
    private String tripType;

    // Distance
    private BigDecimal distancePlanned;
    private BigDecimal distanceActual;

    private Long durationPlannedSeconds;
    private Long durationActualSeconds;

    private String remarks;

    // Timeline
    private List<TripEventResponse> events;

    // Audit
    private Instant createdAt;
    private Instant updatedAt;
    private String createdBy;
}
