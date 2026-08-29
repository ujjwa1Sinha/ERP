package com.transport.erp.document.controller;

import com.transport.erp.common.dto.ApiResponse;
import com.transport.erp.document.dto.DocumentRequest;
import com.transport.erp.document.dto.DocumentResponse;
import com.transport.erp.document.service.DocumentService;
import jakarta.annotation.PostConstruct;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/documents")
@RequiredArgsConstructor
public class DocumentController {

    private final DocumentService documentService;

    @Value("${app.upload.dir:./uploads/documents}")
    private String uploadDir;

    @PostConstruct
    public void init() {
        try {
            Files.createDirectories(Paths.get(uploadDir));
        } catch (IOException e) {
            throw new RuntimeException("Could not create upload directory: " + uploadDir, e);
        }
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAuthority('DOCUMENT_EDIT')")
    public ResponseEntity<ApiResponse<DocumentResponse>> createDocument(
            @RequestParam("entityType") String entityType,
            @RequestParam("entityId") UUID entityId,
            @RequestParam("documentType") String documentType,
            @RequestParam(value = "documentNumber", required = false) String documentNumber,
            @RequestParam(value = "issueDate", required = false) String issueDate,
            @RequestParam(value = "expiryDate", required = false) String expiryDate,
            @RequestParam(value = "remarks", required = false) String remarks,
            @RequestParam(value = "alertDaysBefore", required = false) Integer alertDaysBefore,
            @RequestParam(value = "file", required = false) MultipartFile file) {

        String fileUrl = null;
        if (file != null && !file.isEmpty()) {
            fileUrl = saveFile(file);
        }

        DocumentRequest request = new DocumentRequest();
        request.setEntityType(entityType);
        request.setEntityId(entityId);
        request.setDocumentType(documentType);
        request.setDocumentNumber(documentNumber);
        request.setIssueDate(issueDate != null && !issueDate.isEmpty() ? java.time.LocalDate.parse(issueDate) : null);
        request.setExpiryDate(
                expiryDate != null && !expiryDate.isEmpty() ? java.time.LocalDate.parse(expiryDate) : null);
        request.setFileUrl(fileUrl);
        request.setRemarks(remarks);
        request.setAlertDaysBefore(alertDaysBefore);

        DocumentResponse response = documentService.createDocument(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Document created successfully", response));
    }

    @GetMapping
    @PreAuthorize("hasAuthority('DOCUMENT_VIEW')")
    public ResponseEntity<ApiResponse<List<DocumentResponse>>> getAllDocuments() {
        List<DocumentResponse> response = documentService.getAllActiveDocuments();
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/entity/{entityType}/{entityId}")
    @PreAuthorize("hasAuthority('DOCUMENT_VIEW')")
    public ResponseEntity<ApiResponse<List<DocumentResponse>>> getDocumentsByEntity(
            @PathVariable String entityType, @PathVariable UUID entityId) {
        List<DocumentResponse> response = documentService.getDocumentsByEntity(entityType, entityId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('DOCUMENT_VIEW')")
    public ResponseEntity<ApiResponse<DocumentResponse>> getDocumentById(@PathVariable UUID id) {
        DocumentResponse response = documentService.getDocumentById(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/expiring")
    @PreAuthorize("hasAuthority('DOCUMENT_VIEW')")
    public ResponseEntity<ApiResponse<List<DocumentResponse>>> getExpiringDocuments(
            @RequestParam(defaultValue = "30") int daysAhead) {
        List<DocumentResponse> response = documentService.getExpiringDocuments(daysAhead);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAuthority('DOCUMENT_EDIT')")
    public ResponseEntity<ApiResponse<DocumentResponse>> updateDocument(
            @PathVariable UUID id,
            @RequestParam("entityType") String entityType,
            @RequestParam("entityId") UUID entityId,
            @RequestParam("documentType") String documentType,
            @RequestParam(value = "documentNumber", required = false) String documentNumber,
            @RequestParam(value = "issueDate", required = false) String issueDate,
            @RequestParam(value = "expiryDate", required = false) String expiryDate,
            @RequestParam(value = "remarks", required = false) String remarks,
            @RequestParam(value = "alertDaysBefore", required = false) Integer alertDaysBefore,
            @RequestParam(value = "file", required = false) MultipartFile file) {

        String fileUrl = null;
        if (file != null && !file.isEmpty()) {
            fileUrl = saveFile(file);
        }

        DocumentRequest request = new DocumentRequest();
        request.setEntityType(entityType);
        request.setEntityId(entityId);
        request.setDocumentType(documentType);
        request.setDocumentNumber(documentNumber);
        request.setIssueDate(issueDate != null && !issueDate.isEmpty() ? java.time.LocalDate.parse(issueDate) : null);
        request.setExpiryDate(
                expiryDate != null && !expiryDate.isEmpty() ? java.time.LocalDate.parse(expiryDate) : null);
        request.setFileUrl(fileUrl);
        request.setRemarks(remarks);
        request.setAlertDaysBefore(alertDaysBefore);

        DocumentResponse response = documentService.updateDocument(id, request);
        return ResponseEntity.ok(ApiResponse.success("Document updated successfully", response));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('DOCUMENT_EDIT')")
    public ResponseEntity<ApiResponse<Void>> deleteDocument(@PathVariable UUID id) {
        documentService.deleteDocument(id);
        return ResponseEntity.ok(ApiResponse.success("Document cancelled successfully", null));
    }

    /**
     * Upload a file, validate 1MB limit, store with UUID name.
     */
    private String saveFile(MultipartFile file) {
        // Validate file size (1MB = 1048576 bytes)
        if (file.getSize() > 1_048_576) {
            throw new IllegalArgumentException("File size exceeds 1MB limit. Current size: "
                    + (file.getSize() / 1024) + "KB");
        }

        try {
            String originalFilename = file.getOriginalFilename();
            String extension = "";
            if (originalFilename != null && originalFilename.contains(".")) {
                extension = originalFilename.substring(originalFilename.lastIndexOf("."));
            }
            String storedFilename = UUID.randomUUID() + extension;

            Path targetPath = Paths.get(uploadDir).resolve(storedFilename);
            Files.copy(file.getInputStream(), targetPath, StandardCopyOption.REPLACE_EXISTING);

            return "/api/documents/files/" + storedFilename;
        } catch (IOException e) {
            throw new RuntimeException("Failed to store file", e);
        }
    }

    @GetMapping("/files/{filename:.+}")
    public ResponseEntity<org.springframework.core.io.Resource> serveFile(@PathVariable String filename) {
        try {
            Path file = Paths.get(uploadDir).resolve(filename).normalize();
            org.springframework.core.io.Resource resource = new org.springframework.core.io.UrlResource(file.toUri());
            if (resource.exists() && resource.isReadable()) {
                String contentType = Files.probeContentType(file);
                if (contentType == null)
                    contentType = "application/octet-stream";
                return ResponseEntity.ok()
                        .contentType(MediaType.parseMediaType(contentType))
                        .header("Content-Disposition", "inline; filename=\"" + filename + "\"")
                        .body(resource);
            }
            return ResponseEntity.notFound().build();
        } catch (IOException e) {
            return ResponseEntity.internalServerError().build();
        }
    }
}
