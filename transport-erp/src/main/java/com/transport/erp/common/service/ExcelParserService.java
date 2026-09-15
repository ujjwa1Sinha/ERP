package com.transport.erp.common.service;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.*;

@Service
public class ExcelParserService {

    /**
     * Parses an .xlsx file into a list of row maps.
     * The first row is treated as the header row; keys are lowercased and trimmed.
     */
    public List<Map<String, String>> parse(MultipartFile file) throws IOException {
        List<Map<String, String>> rows = new ArrayList<>();

        try (InputStream is = file.getInputStream();
                Workbook workbook = new XSSFWorkbook(is)) {

            Sheet sheet = workbook.getSheetAt(0);
            if (sheet.getPhysicalNumberOfRows() < 2) {
                return rows; // header only or empty
            }

            // Read header row
            Row headerRow = sheet.getRow(0);
            List<String> headers = new ArrayList<>();
            for (int c = 0; c < headerRow.getLastCellNum(); c++) {
                Cell cell = headerRow.getCell(c, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);
                headers.add(getCellStringValue(cell).trim().toLowerCase().replace(" ", "_"));
            }

            // Read data rows
            for (int r = 1; r <= sheet.getLastRowNum(); r++) {
                Row row = sheet.getRow(r);
                if (row == null || isRowEmpty(row))
                    continue;

                Map<String, String> rowData = new LinkedHashMap<>();
                for (int c = 0; c < headers.size(); c++) {
                    Cell cell = row.getCell(c, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);
                    rowData.put(headers.get(c), getCellStringValue(cell).trim());
                }
                rows.add(rowData);
            }
        }
        return rows;
    }

    /**
     * Returns the expected headers for a given entity type (used for template
     * generation).
     */
    public List<String> getTemplateHeaders(String entityType) {
        return switch (entityType.toLowerCase()) {
            case "drivers" -> List.of(
                    "employee_code", "name", "phone", "alternate_phone",
                    "date_of_birth", "joining_date", "status",
                    "address", "city", "state", "pin_code",
                    "aadhar_number", "pan_number", "blood_group",
                    "license_number", "license_type", "license_issuing_authority",
                    "license_issue_date", "license_expiry_date",
                    "ec_name", "ec_relationship", "ec_phone", "ec_alternate_phone", "ec_address",
                    "branch_name");
            case "vehicles" -> List.of(
                    "registration_number", "vehicle_type", "make", "model",
                    "year", "fuel_type", "capacity",
                    "chassis_number", "engine_number", "gps_device_id",
                    "insurance_expiry", "fitness_expiry", "permit_expiry",
                    "pollution_expiry", "tax_expiry", "branch_name");
            case "users" -> List.of(
                    "username", "password", "email", "full_name",
                    "phone", "role", "branch_name");
            case "branches" -> List.of(
                    "name", "code", "address", "city", "state",
                    "pin_code", "phone", "email", "contact_person");
            default -> List.of();
        };
    }

    private String getCellStringValue(Cell cell) {
        if (cell == null)
            return "";
        return switch (cell.getCellType()) {
            case STRING -> cell.getStringCellValue();
            case NUMERIC -> {
                if (DateUtil.isCellDateFormatted(cell)) {
                    Date date = cell.getDateCellValue();
                    yield new SimpleDateFormat("yyyy-MM-dd").format(date);
                }
                double val = cell.getNumericCellValue();
                if (val == Math.floor(val) && !Double.isInfinite(val)) {
                    yield String.valueOf((long) val);
                }
                yield String.valueOf(val);
            }
            case BOOLEAN -> String.valueOf(cell.getBooleanCellValue());
            case FORMULA -> {
                try {
                    yield cell.getStringCellValue();
                } catch (Exception e) {
                    yield String.valueOf(cell.getNumericCellValue());
                }
            }
            default -> "";
        };
    }

    private boolean isRowEmpty(Row row) {
        for (int c = row.getFirstCellNum(); c < row.getLastCellNum(); c++) {
            Cell cell = row.getCell(c);
            if (cell != null && cell.getCellType() != CellType.BLANK
                    && !getCellStringValue(cell).trim().isEmpty()) {
                return false;
            }
        }
        return true;
    }
}
