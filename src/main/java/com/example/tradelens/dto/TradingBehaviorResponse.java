package com.example.tradelens.dto;

public record TradingBehaviorResponse(
        long totalTrades,
        long earlyExits,
        long lateEntries,
        long stopLossHits,
        long targetHits
) {
}
