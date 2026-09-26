package com.example.tradelens.service;


import com.example.tradelens.dto.DecisionVsActualResponse;
import com.example.tradelens.entity.Decision;
import com.example.tradelens.entity.Trade;
import com.example.tradelens.repository.TradeRepository;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

import java.math.BigDecimal;


@Service
@RequiredArgsConstructor
public class DecisionVsActualService {

    private final TradeRepository tradeRepository;

    public DecisionVsActualResponse analyze(Long tradeId) {

        Trade trade = tradeRepository
                .findById(tradeId)
                .orElseThrow(() -> new IllegalArgumentException("Trade not found"));

        Decision decision = trade.getDecision();

        if(decision == null) {
            throw new IllegalArgumentException("Decision not found for trade");
        }

        BigDecimal entryDeviation = trade.getEntryPrice()
                .subtract(decision.getPlannedEntry());

        BigDecimal exitDeviation = trade.getExitPrice()
                .subtract(decision.getTarget());

        BigDecimal plannedRisk = decision.getPlannedEntry()
                .subtract(decision.getStopLoss()).abs();

        BigDecimal actualProfitLoss = trade.getProfitLoss();

        boolean targetAchieved =
                trade.getExitPrice().compareTo(decision.getTarget()) >= 0;

        boolean stopLossHit =
                trade.getExitPrice().compareTo(decision.getStopLoss()) <= 0;

        return new DecisionVsActualResponse(
                decision.getPlannedEntry(),
                trade.getEntryPrice(),
                entryDeviation,

                decision.getTarget(),
                trade.getExitPrice(),
                exitDeviation,

                decision.getStopLoss(),
                plannedRisk,

                actualProfitLoss,

                targetAchieved,
                stopLossHit
        );

    }

}
