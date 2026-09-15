package com.transport.erp.common.controller;

import com.transport.erp.common.dto.ApiResponse;
import com.transport.erp.common.dto.BulkImportResult;
import com.transport.erp.common.service.BulkImportService;
import com.transport.erp.common.service.ExcelParserService;
import lombok.RequiredArgsConstructor;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/import")
@RequiredArgsConstructor
public class BulkImportController {

    private final ExcelParserService excelParserService;
    private final BulkImportService bulkImportService;

    private static final long MAX_IMPORT_SIZE = 5 * 1024 * 1024; // 5MB

    @PostMapping(value = "/{entityType}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAuthority('DATA_IMPORT')")
    public ResponseEntity<ApiResponse<BulkImportResult>> importExcel(
            @PathVariable String entityType,
            @RequestParam("file") MultipartFile file) throws Exception {

        // Validate file
        if (file.isEmpty()) {
            throw new IllegalArgumentException("File is empty");
        }
        if (file.getSize() > MAX_IMPORT_SIZE) {
            throw new IllegalArgumentException("File exceeds 5MB limit");
        }
        String filename = file.getOriginalFilename();
        if (filename == null || !filename.toLowerCase().endsWith(".xlsx")) {
            throw new IllegalArgumentException("Only .xlsx files are accepted");
        }

        // Parse and import
        List<Map<String, String>> rows = excelParserService.parse(file);
        if (rows.isEmpty()) {
            return ResponseEntity.ok(ApiResponse.success("No data rows found in the file",
                    BulkImportResult.builder().totalRows(0).successCount(0).errorCount(0).build()));
        }

        BulkImportResult result = bulkImportService.importData(entityType, rows);

        String message = String.format("Import complete: %d of %d rows imported successfully",
                result.getSuccessCount(), result.getTotalRows());
        return ResponseEntity.ok(ApiResponse.success(message, result));
    }

    @GetMapping("/{entityType}/template")
    @PreAuthorize("hasAuthority('DATA_IMPORT')")
    public ResponseEntity<byte[]> downloadTemplate(@PathVariable String entityType) throws Exception {
        List<String> headers = excelParserService.getTemplateHeaders(entityType);
        if (headers.isEmpty()) {
            throw new IllegalArgumentException("Unsupported entity type: " + entityType);
        }

        try (XSSFWorkbook workbook = new XSSFWorkbook()) {
            XSSFSheet sheet = workbook.createSheet(entityType);
            XSSFRow headerRow = sheet.createRow(0);

            // Style header
            var style = workbook.createCellStyle();
            var font = workbook.createFont();
            font.setBold(true);
            style.setFont(font);

            for (int i = 0; i < headers.size(); i++) {
                var cell = headerRow.createCell(i);
                cell.setCellValue(headers.get(i));
                cell.setCellStyle(style);
                sheet.setColumnWidth(i, 5000);
            }

            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            workbook.write(bos);

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION,
                            "attachment; filename=" + entityType + "_template.xlsx")
                    .contentType(MediaType.parseMediaType(
                            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                    .body(bos.toByteArray());
        }
    }
}
