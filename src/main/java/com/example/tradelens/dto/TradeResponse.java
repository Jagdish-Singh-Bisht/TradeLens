package com.example.tradelens.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record TradeResponse(
        Long id,
        String symbol,
        String exchange,
        BigDecimal quantity,
        BigDecimal entryPrice,
        BigDecimal exitPrice,
        LocalDateTime entryTime,
        LocalDateTime exitTime,
        BigDecimal profitLoss,
        DecisionResponse decision
) {
    public record DecisionResponse(
            String strategy,
            BigDecimal plannedEntry,
            BigDecimal target,
            BigDecimal stopLoss,
            Integer confidence,
            String entryReason,
            String exitReason
    ) {}
}
