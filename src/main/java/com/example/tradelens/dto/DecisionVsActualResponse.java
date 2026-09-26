package com.example.tradelens.dto;

import java.math.BigDecimal;

public record DecisionVsActualResponse(

        BigDecimal plannedEntry,
        BigDecimal actualEntry,
        BigDecimal entryDeviation,

        BigDecimal target,
        BigDecimal actualExit,
        BigDecimal exitDeviation,

        BigDecimal stopLoss,
        BigDecimal plannedRisk,

        BigDecimal actualProfitLoss,

        boolean targetAchieved,
        boolean stopLossHit

) {

}
