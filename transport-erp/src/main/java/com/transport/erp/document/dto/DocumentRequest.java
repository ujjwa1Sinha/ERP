package com.transport.erp.document.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DocumentRequest {

    @NotNull(message = "Entity type is required")
    private String entityType;

    @NotNull(message = "Entity ID is required")
    private UUID entityId;

    @NotNull(message = "Document type is required")
    private String documentType;

    private String documentNumber;
    private LocalDate issueDate;
    private LocalDate expiryDate;
    private String fileUrl;
    private String remarks;
    private Integer alertDaysBefore;
}
