package com.transport.erp.common.dto;

import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BulkImportResult {

    private int totalRows;
    private int successCount;
    private int errorCount;

    @Builder.Default
    private List<RowError> errors = new ArrayList<>();

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RowError {
        private int row;
        private String column;
        private String message;
    }
}
