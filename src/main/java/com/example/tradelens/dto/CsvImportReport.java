package com.example.tradelens.dto;

import java.util.List;

public record CsvImportReport(
        int totalRows,
        int importedRows,
        int skippedRows,
        int rejectedRows,
        List<RowError> errors
) {
    public record RowError(
            long rowNumber,
            String reason
    ) { }
}
