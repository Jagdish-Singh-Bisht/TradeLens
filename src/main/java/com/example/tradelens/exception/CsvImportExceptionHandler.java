package com.example.tradelens.exception;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

@RestControllerAdvice
public class CsvImportExceptionHandler {

    @ExceptionHandler(InvalidCsvException.class)
    public ResponseEntity<Map<String, String>> handleInvalidCsv(
            InvalidCsvException exception) {

        return ResponseEntity
                .badRequest()
                .body(Map.of("error", exception.getMessage()));
    }
}