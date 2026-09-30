package com.example.tradelens.controller;


import com.example.tradelens.service.MarketCandleCsvImportService;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import org.springframework.web.multipart.MultipartFile;

import lombok.RequiredArgsConstructor;




@RestController
@RequestMapping("/api/import")
@RequiredArgsConstructor
public class MarketCandleImportController {

    private final MarketCandleCsvImportService marketCandleCsvImportService;

    @PostMapping("/market-candles")
    public String importMarketCandles(@RequestParam("file") MultipartFile file) {

        marketCandleCsvImportService.importCandles(file);

        return "Market candle CSV imported successfully";
    }

}
