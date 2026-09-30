package com.example.tradelens.dto;

import java.math.BigDecimal;

public record MfeResponse(
        BigDecimal entryPrice,
        BigDecimal highestPrice,
        BigDecimal mfe
){
}
