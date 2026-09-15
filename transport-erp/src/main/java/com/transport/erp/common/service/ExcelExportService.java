package com.transport.erp.common.service;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.*;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Map;

@Service
public class ExcelExportService {

    /**
     * Generates an .xlsx workbook from column definitions and row data.
     *
     * @param sheetName Name of the Excel sheet
     * @param columns   Ordered list of column keys (used as headers)
     * @param data      List of row maps
     * @return byte array of the generated .xlsx file
     */
    public byte[] generate(String sheetName, List<String> columns, List<Map<String, Object>> data)
            throws IOException {

        try (XSSFWorkbook workbook = new XSSFWorkbook()) {
            XSSFSheet sheet = workbook.createSheet(sheetName);

            // ── Header style ───────────────────────────────────────────
            XSSFCellStyle headerStyle = workbook.createCellStyle();
            XSSFFont headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerFont.setFontHeightInPoints((short) 11);
            headerStyle.setFont(headerFont);
            headerStyle.setFillForegroundColor(IndexedColors.LIGHT_CORNFLOWER_BLUE.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            headerStyle.setBorderBottom(BorderStyle.THIN);

            // ── Data style ─────────────────────────────────────────────
            XSSFCellStyle dateStyle = workbook.createCellStyle();
            CreationHelper createHelper = workbook.getCreationHelper();
            dateStyle.setDataFormat(createHelper.createDataFormat().getFormat("yyyy-mm-dd"));

            // ── Write header ───────────────────────────────────────────
            XSSFRow headerRow = sheet.createRow(0);
            for (int c = 0; c < columns.size(); c++) {
                XSSFCell cell = headerRow.createCell(c);
                cell.setCellValue(formatHeader(columns.get(c)));
                cell.setCellStyle(headerStyle);
            }

            // ── Write data ─────────────────────────────────────────────
            for (int r = 0; r < data.size(); r++) {
                XSSFRow row = sheet.createRow(r + 1);
                Map<String, Object> rowData = data.get(r);
                for (int c = 0; c < columns.size(); c++) {
                    XSSFCell cell = row.createCell(c);
                    Object value = rowData.get(columns.get(c));
                    setCellValue(cell, value, dateStyle);
                }
            }

            // Auto-size columns
            for (int c = 0; c < columns.size(); c++) {
                sheet.autoSizeColumn(c);
                // Minimum width
                if (sheet.getColumnWidth(c) < 3000) {
                    sheet.setColumnWidth(c, 3000);
                }
            }

            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            workbook.write(bos);
            return bos.toByteArray();
        }
    }

    private void setCellValue(XSSFCell cell, Object value, XSSFCellStyle dateStyle) {
        if (value == null) {
            cell.setCellValue("");
        } else if (value instanceof Number num) {
            cell.setCellValue(num.doubleValue());
        } else if (value instanceof Boolean bool) {
            cell.setCellValue(bool);
        } else if (value instanceof java.time.LocalDate date) {
            cell.setCellValue(date.toString());
        } else if (value instanceof java.time.LocalDateTime dt) {
            cell.setCellValue(dt.toString());
        } else if (value instanceof java.time.Instant inst) {
            cell.setCellValue(inst.toString());
        } else {
            cell.setCellValue(value.toString());
        }
    }

    /** Converts snake_case keys to Title Case for headers. */
    private String formatHeader(String key) {
        if (key == null)
            return "";
        String[] parts = key.split("_");
        StringBuilder sb = new StringBuilder();
        for (String part : parts) {
            if (!part.isEmpty()) {
                sb.append(Character.toUpperCase(part.charAt(0)))
                        .append(part.substring(1)).append(" ");
            }
        }
        return sb.toString().trim();
    }
}
