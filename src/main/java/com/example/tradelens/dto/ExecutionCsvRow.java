package com.example.tradelens.dto;


import java.math.BigDecimal;
import java.time.LocalDateTime;



public record ExecutionCsvRow (
        String orderId,
        String symbol,
        String exchange,
        String side,
        BigDecimal quantity,
        BigDecimal price,
        LocalDateTime executedAt
) {

}



