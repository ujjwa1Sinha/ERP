package com.transport.erp.document.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DocumentResponse {

    private UUID id;
    private String entityType;
    private UUID entityId;
    private String documentType;
    private String documentNumber;
    private LocalDate issueDate;
    private LocalDate expiryDate;
    private String fileUrl;
    private String remarks;
    private String status;
    private Integer alertDaysBefore;
    private long daysUntilExpiry;
    private Instant createdAt;
}
