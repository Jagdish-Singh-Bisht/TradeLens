package com.example.tradelens.dto;



import java.math.BigDecimal;


public record TradeAnalyticsResponse (

        long totalTrades,
        long winningTrades,
        long losingTrades,
        BigDecimal winRate,
        BigDecimal totalProfitLoss,
        BigDecimal averageProfit,
        BigDecimal averageLoss,
        BigDecimal profitFactor

) {

}
