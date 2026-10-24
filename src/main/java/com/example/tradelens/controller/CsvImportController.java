package com.example.tradelens.controller;



import com.example.tradelens.dto.CsvImportReport;
import com.example.tradelens.repository.BrokerAccountRepository;
import com.example.tradelens.service.CsvImportService;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import lombok.RequiredArgsConstructor;
import org.springframework.web.server.ResponseStatusException;


@RestController
@RequestMapping("/api/import")
@RequiredArgsConstructor
public class CsvImportController {

    private final CsvImportService csvImportService;

    private final BrokerAccountRepository brokerAccountRepository;

    @PostMapping("/executions")
    public CsvImportReport importExecutions(
            @RequestParam("brokerAccountId") Long brokerAccountId,
            @RequestParam("file") MultipartFile file,
            Authentication authentication) {

        return csvImportService.importExecutions(
                brokerAccountId,
                file,
                authentication.getName()
        );

    }

}
