package com.transport.erp.document.repository;

import com.transport.erp.document.domain.Document;
import com.transport.erp.document.domain.DocumentStatus;
import com.transport.erp.document.domain.DocumentType;
import com.transport.erp.document.domain.EntityType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Repository
public interface DocumentRepository extends JpaRepository<Document, UUID> {

    List<Document> findByEntityTypeAndEntityId(EntityType entityType, UUID entityId);

    List<Document> findByEntityTypeAndEntityIdAndDocumentType(
            EntityType entityType, UUID entityId, DocumentType documentType);

    @Query("SELECT d FROM Document d WHERE d.expiryDate <= :date AND d.status = 'ACTIVE'")
    List<Document> findExpiringDocuments(@Param("date") LocalDate date);

    @Query("SELECT d FROM Document d WHERE d.expiryDate BETWEEN :startDate AND :endDate AND d.status = 'ACTIVE'")
    List<Document> findDocumentsExpiringBetween(
            @Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    List<Document> findByStatus(DocumentStatus status);
}
