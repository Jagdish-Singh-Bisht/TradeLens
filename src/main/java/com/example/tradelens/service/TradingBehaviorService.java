package com.example.tradelens.service;


import com.example.tradelens.dto.TradingBehaviorResponse;
import com.example.tradelens.entity.BrokerAccount;
import com.example.tradelens.entity.Trade;
import com.example.tradelens.repository.TradeRepository;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

import java.util.List;


@Service
@RequiredArgsConstructor
public class TradingBehaviorService {

    private final TradeRepository tradeRepository;

    public TradingBehaviorResponse getBehavior(BrokerAccount brokerAccount) {

        List<Trade> trades = tradeRepository
                .findByBrokerAccount(brokerAccount);

        long totalTrades = trades.stream()
                .filter(trade -> trade.getDecision() != null)
                .count();

        long earlyExits = trades.stream()
                .filter(this::isEarlyExit)
                .count();

        long lateEntries = trades.stream()
                .filter(this::isLateEntry)
                .count();

        long stopLossHits = trades.stream()
                .filter(this::isStopLossHit)
                .count();

        long targetHits = trades.stream()
                .filter(this::isTargetHit)
                .count();

        return new TradingBehaviorResponse(
                totalTrades,
                earlyExits,
                lateEntries,
                stopLossHits,
                targetHits
        );

    }

    private boolean isEarlyExit(Trade trade) {

        if (trade.getDecision() == null) {
            return false;
        }

        return trade.getExitPrice()
                .compareTo(trade.getDecision().getTarget()) < 0;
    }

    private boolean isLateEntry(Trade trade) {

        if (trade.getDecision() == null) {
            return false;
        }

        return trade.getEntryPrice()
                .compareTo(trade.getDecision().getPlannedEntry()) > 0;
    }

    private boolean isStopLossHit(Trade trade) {

        if (trade.getDecision() == null) {
            return false;
        }

        return trade.getExitPrice()
                .compareTo(trade.getDecision().getStopLoss()) <= 0;
    }

    private boolean isTargetHit(Trade trade) {

        if (trade.getDecision() == null) {
            return false;
        }

        return trade.getExitPrice()
                .compareTo(trade.getDecision().getTarget()) >= 0;
    }

}
