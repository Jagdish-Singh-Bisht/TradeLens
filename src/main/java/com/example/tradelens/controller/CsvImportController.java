package com.example.tradelens.controller;



import com.example.tradelens.service.CsvImportService;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import lombok.RequiredArgsConstructor;



@RestController
@RequestMapping("/api/import")
@RequiredArgsConstructor
public class CsvImportController {

    private final CsvImportService csvImportService;

    @PostMapping("/executions")
    public String importExecutions(
            @RequestParam("brokerAccountId") Long brokerAccountId,
            @RequestParam("file") MultipartFile file) {

        csvImportService.importExecutions(
                brokerAccountId,
                file
        );

        return "CSV received";
    }

}
