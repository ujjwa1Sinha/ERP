package com.transport.erp.document.service;

import com.transport.erp.common.exception.ResourceNotFoundException;
import com.transport.erp.document.domain.*;
import com.transport.erp.document.dto.DocumentRequest;
import com.transport.erp.document.dto.DocumentResponse;
import com.transport.erp.document.repository.DocumentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DocumentService {

    private final DocumentRepository documentRepository;

    @Transactional
    public DocumentResponse createDocument(DocumentRequest request) {
        Document document = Document.builder()
                .entityType(EntityType.valueOf(request.getEntityType().toUpperCase()))
                .entityId(request.getEntityId())
                .documentType(DocumentType.valueOf(request.getDocumentType().toUpperCase()))
                .documentNumber(request.getDocumentNumber())
                .issueDate(request.getIssueDate())
                .expiryDate(request.getExpiryDate())
                .fileUrl(request.getFileUrl())
                .remarks(request.getRemarks())
                .alertDaysBefore(request.getAlertDaysBefore() != null ? request.getAlertDaysBefore() : 30)
                .status(DocumentStatus.ACTIVE)
                .build();

        return mapToResponse(documentRepository.save(document));
    }

    @Transactional(readOnly = true)
    public List<DocumentResponse> getAllActiveDocuments() {
        return documentRepository.findByStatus(DocumentStatus.ACTIVE)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<DocumentResponse> getDocumentsByEntity(String entityType, UUID entityId) {
        return documentRepository.findByEntityTypeAndEntityId(
                EntityType.valueOf(entityType.toUpperCase()), entityId)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public DocumentResponse getDocumentById(UUID id) {
        Document document = documentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Document", "id", id));
        return mapToResponse(document);
    }

    @Transactional(readOnly = true)
    public List<DocumentResponse> getExpiringDocuments(int daysAhead) {
        LocalDate targetDate = LocalDate.now().plusDays(daysAhead);
        return documentRepository.findDocumentsExpiringBetween(LocalDate.now(), targetDate)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public DocumentResponse updateDocument(UUID id, DocumentRequest request) {
        Document document = documentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Document", "id", id));

        document.setDocumentType(DocumentType.valueOf(request.getDocumentType().toUpperCase()));
        document.setDocumentNumber(request.getDocumentNumber());
        document.setIssueDate(request.getIssueDate());
        document.setExpiryDate(request.getExpiryDate());
        document.setFileUrl(request.getFileUrl());
        document.setRemarks(request.getRemarks());
        if (request.getAlertDaysBefore() != null) {
            document.setAlertDaysBefore(request.getAlertDaysBefore());
        }

        return mapToResponse(documentRepository.save(document));
    }

    @Transactional
    public void deleteDocument(UUID id) {
        Document document = documentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Document", "id", id));
        document.setStatus(DocumentStatus.CANCELLED);
        documentRepository.save(document);
    }

    private DocumentResponse mapToResponse(Document document) {
        long daysUntilExpiry = document.getExpiryDate() != null
                ? ChronoUnit.DAYS.between(LocalDate.now(), document.getExpiryDate())
                : Long.MAX_VALUE;

        return DocumentResponse.builder()
                .id(document.getId())
                .entityType(document.getEntityType().name())
                .entityId(document.getEntityId())
                .documentType(document.getDocumentType().name())
                .documentNumber(document.getDocumentNumber())
                .issueDate(document.getIssueDate())
                .expiryDate(document.getExpiryDate())
                .fileUrl(document.getFileUrl())
                .remarks(document.getRemarks())
                .status(document.getStatus().name())
                .alertDaysBefore(document.getAlertDaysBefore())
                .daysUntilExpiry(daysUntilExpiry)
                .createdAt(document.getCreatedAt())
                .build();
    }
}
