package com.example.tradelens.dto;

public record StrategyAnalyticsResponse(

        String strategy,
        TradeAnalyticsResponse analytics

) {
}
