package com.transport.erp.common.service;

import com.lowagie.text.*;
import com.lowagie.text.Font;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import org.springframework.stereotype.Service;

import java.awt.*;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

@Service
public class PdfExportService {

    private static final Font TITLE_FONT = new Font(Font.HELVETICA, 16, Font.BOLD, new Color(33, 37, 41));
    private static final Font SUBTITLE_FONT = new Font(Font.HELVETICA, 9, Font.NORMAL, new Color(108, 117, 125));
    private static final Font HEADER_FONT = new Font(Font.HELVETICA, 9, Font.BOLD, Color.WHITE);
    private static final Font CELL_FONT = new Font(Font.HELVETICA, 8, Font.NORMAL, new Color(33, 37, 41));
    private static final Color HEADER_BG = new Color(59, 130, 246); // blue-500
    private static final Color ALT_ROW_BG = new Color(243, 244, 246); // gray-100

    /**
     * Generates a styled PDF table report.
     *
     * @param title   Report title
     * @param columns Column keys
     * @param data    Row data
     * @return PDF as byte array
     */
    public byte[] generate(String title, List<String> columns, List<Map<String, Object>> data)
            throws IOException {

        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        Document document = new Document(PageSize.A4.rotate(), 20, 20, 30, 30);
        PdfWriter.getInstance(document, bos);
        document.open();

        // ── Title ──────────────────────────────────────────────────────
        Paragraph titlePara = new Paragraph(title, TITLE_FONT);
        titlePara.setAlignment(Element.ALIGN_LEFT);
        document.add(titlePara);

        String timestamp = "Generated: " + LocalDateTime.now()
                .format(DateTimeFormatter.ofPattern("dd MMM yyyy, hh:mm a"));
        Paragraph datePara = new Paragraph(timestamp, SUBTITLE_FONT);
        datePara.setSpacingAfter(12);
        document.add(datePara);

        // ── Table ──────────────────────────────────────────────────────
        PdfPTable table = new PdfPTable(columns.size());
        table.setWidthPercentage(100);
        table.setSpacingBefore(5);

        // Header row
        for (String col : columns) {
            PdfPCell cell = new PdfPCell(new Phrase(formatHeader(col), HEADER_FONT));
            cell.setBackgroundColor(HEADER_BG);
            cell.setPadding(6);
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            cell.setBorderWidth(0);
            table.addCell(cell);
        }

        // Data rows
        for (int r = 0; r < data.size(); r++) {
            Map<String, Object> row = data.get(r);
            for (String col : columns) {
                Object value = row.get(col);
                PdfPCell cell = new PdfPCell(new Phrase(
                        value != null ? value.toString() : "—", CELL_FONT));
                cell.setPadding(5);
                cell.setBorderWidth(0.5f);
                cell.setBorderColor(new Color(229, 231, 235));
                if (r % 2 == 1) {
                    cell.setBackgroundColor(ALT_ROW_BG);
                }
                table.addCell(cell);
            }
        }

        document.add(table);

        // ── Footer ─────────────────────────────────────────────────────
        Paragraph footer = new Paragraph(
                String.format("Total Records: %d", data.size()), SUBTITLE_FONT);
        footer.setSpacingBefore(10);
        document.add(footer);

        document.close();
        return bos.toByteArray();
    }

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
