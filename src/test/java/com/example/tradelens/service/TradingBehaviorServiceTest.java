package com.example.tradelens.service;

import com.example.tradelens.dto.TradingBehaviorResponse;
import com.example.tradelens.entity.BrokerAccount;
import com.example.tradelens.entity.Decision;
import com.example.tradelens.entity.Trade;
import com.example.tradelens.repository.TradeRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TradingBehaviorServiceTest {

    @Mock
    private TradeRepository tradeRepository;

    @InjectMocks
    private TradingBehaviorService tradingBehaviorService;

    @Test
    void shouldCalculateTradingBehavior() {

        BrokerAccount brokerAccount = new BrokerAccount();
        brokerAccount.setId(1L);

        Trade earlyExit = createTrade(
                "BREAKOUT",
                "100",
                "115",
                "120",
                "90"
        );

        Trade targetHit = createTrade(
                "BREAKOUT",
                "100",
                "120",
                "120",
                "90"
        );

        Trade lateEntry = createTrade(
                "REVERSAL",
                "100",
                "120",
                "130",
                "90"
        );

        lateEntry.setEntryPrice(new BigDecimal("110"));

        Trade stopLossHit = createTrade(
                "REVERSAL",
                "100",
                "90",
                "120",
                "90"
        );

        when(tradeRepository.findByBrokerAccount(brokerAccount))
                .thenReturn(List.of(
                        earlyExit,
                        targetHit,
                        lateEntry,
                        stopLossHit
                ));

        TradingBehaviorResponse result =
                tradingBehaviorService.getBehavior(brokerAccount);

        assertEquals(4, result.totalTrades());
        assertEquals(3, result.earlyExits());
        assertEquals(1, result.lateEntries());
        assertEquals(1, result.stopLossHits());
        assertEquals(1, result.targetHits());
    }

    private Trade createTrade(
            String strategy,
            String plannedEntry,
            String exitPrice,
            String target,
            String stopLoss ) {

        Decision decision = new Decision();

        decision.setStrategy(strategy);
        decision.setPlannedEntry(
                new BigDecimal(plannedEntry)
        );
        decision.setTarget(
                new BigDecimal(target)
        );
        decision.setStopLoss(
                new BigDecimal(stopLoss)
        );

        Trade trade = new Trade();

        trade.setDecision(decision);
        trade.setEntryPrice(
                new BigDecimal(plannedEntry)
        );
        trade.setExitPrice(
                new BigDecimal(exitPrice)
        );

        return trade;
    }
}