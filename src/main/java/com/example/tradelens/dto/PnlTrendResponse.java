package com.example.tradelens.dto;

import java.math.BigDecimal;

public record PnlTrendResponse(
        int tradeNumber,
        BigDecimal profitLoss,
        BigDecimal cumulativeProfitLoss
) {
}
