package com.example.tradelens.dto;

import java.math.BigDecimal;

public record RiskAnalyticsResponse(
        long totalTrades,
        BigDecimal netProfitLoss,
        BigDecimal expectancy,
        BigDecimal maximumDrawdown,
        long maximumConsecutiveWins,
        long maximumConsecutiveLosses,
        BigDecimal recoveryFactor
) {
}
