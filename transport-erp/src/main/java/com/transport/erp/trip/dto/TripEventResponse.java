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
public class TripEventResponse {

    private UUID id;
    private String eventType;
    private Instant eventTimestamp;
    private BigDecimal latitude;
    private BigDecimal longitude;
    private String remarks;
    private String createdBy;
}
