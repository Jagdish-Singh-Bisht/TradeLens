package com.example.tradelens.dto;


import java.math.BigDecimal;

public record MarketContextResponse(
        Long tradeId,
        String symbol,
        BigDecimal entryPrice,
        BigDecimal exitPrice,
        BigDecimal mfe,
        BigDecimal mae,
        BigDecimal postExitMovement
) {
}