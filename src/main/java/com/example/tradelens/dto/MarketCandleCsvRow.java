package com.example.tradelens.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record MarketCandleCsvRow(
        String symbol,
        String exchange,
        LocalDateTime candleTime,
        BigDecimal open,
        BigDecimal high,
        BigDecimal low,
        BigDecimal close,
        Long volume
) {
}