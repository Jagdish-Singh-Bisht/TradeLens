package com.example.tradelens.controller;


import com.example.tradelens.dto.PnlTrendResponse;
import com.example.tradelens.dto.StrategyAnalyticsResponse;
import com.example.tradelens.dto.TradeAnalyticsResponse;
import com.example.tradelens.service.StrategyAnalyticsService;
import com.example.tradelens.service.TradeAnalyticsService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

import java.time.LocalDate;
import java.util.List;


@RestController
@RequestMapping("/api/analytics")
@RequiredArgsConstructor
public class ConsolidatedAnalyticsController {

    private final TradeAnalyticsService tradeAnalyticsService;

    private final StrategyAnalyticsService strategyAnalyticsService;

    @GetMapping("/consolidated")
    public TradeAnalyticsResponse getConsolidatedAnalytics(Authentication authentication,
                                                           @RequestParam(required = false)
                                                           @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
                                                           LocalDate startDate,

                                                           @RequestParam(required = false)
                                                           @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
                                                           LocalDate endDate) {

        return tradeAnalyticsService.getConsolidatedAnalytics(
                authentication.getName(), startDate, endDate
        );
    }

    @GetMapping("/consolidated/strategies")
    public List<StrategyAnalyticsResponse> getConsolidatedStrategyAnalytics(Authentication authentication,
                                                                            @RequestParam(required = false)
                                                                            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
                                                                            LocalDate startDate,

                                                                            @RequestParam(required = false)
                                                                            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
                                                                            LocalDate endDate ) {

        return strategyAnalyticsService.getConsolidatedAnalytics(
                authentication.getName(), startDate, endDate);

    }

    @GetMapping("/consolidated/pnl-trend")
    public List<PnlTrendResponse> getConsolidatedPnlTrend(Authentication authentication,
                                                          @RequestParam(required = false)
                                                          @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
                                                          LocalDate startDate,

                                                          @RequestParam(required = false)
                                                          @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
                                                          LocalDate endDate ) {

        return tradeAnalyticsService.getConsolidatedPnlTrend(
                authentication.getName(), startDate, endDate);

    }
}
