package com.example.tradelens.controller;


import com.example.tradelens.dto.PnlTrendResponse;
import com.example.tradelens.dto.StrategyAnalyticsResponse;
import com.example.tradelens.dto.TradeAnalyticsResponse;
import com.example.tradelens.service.StrategyAnalyticsService;
import com.example.tradelens.service.TradeAnalyticsService;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

import java.util.List;


@RestController
@RequestMapping("/api/analytics")
@RequiredArgsConstructor
public class ConsolidatedAnalyticsController {

    private final TradeAnalyticsService tradeAnalyticsService;

    private final StrategyAnalyticsService strategyAnalyticsService;

    @GetMapping("/consolidated")
    public TradeAnalyticsResponse getConsolidatedAnalytics(Authentication authentication) {

        return tradeAnalyticsService.getConsolidatedAnalytics(
                authentication.getName()
        );
    }

    @GetMapping("/consolidated/strategies")
    public List<StrategyAnalyticsResponse> getConsolidatedStrategyAnalytics(
            Authentication authentication) {

        return strategyAnalyticsService.getConsolidatedAnalytics(
                authentication.getName()
        );
    }

    @GetMapping("/consolidated/pnl-trend")
    public List<PnlTrendResponse> getConsolidatedPnlTrend(
            Authentication authentication) {

        return tradeAnalyticsService.getConsolidatedPnlTrend(
                authentication.getName()
        );
    }
}
