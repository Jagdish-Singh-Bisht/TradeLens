package com.example.tradelens.dto;

import java.math.BigDecimal;

public record MaeResponse(
        BigDecimal entryPrice,
        BigDecimal lowestPrice,
        BigDecimal mae
) {

}