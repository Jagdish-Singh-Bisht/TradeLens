package com.example.tradelens.service;


import com.example.tradelens.entity.Decision;
import com.example.tradelens.dto.StrategyAnalyticsResponse;
import com.example.tradelens.dto.TradeAnalyticsResponse;
import com.example.tradelens.entity.Trade;
import com.example.tradelens.entity.BrokerAccount;
import com.example.tradelens.repository.TradeRepository;

import org.mockito.Mock;
import org.mockito.InjectMocks;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.when;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import java.math.BigDecimal;



@ExtendWith(MockitoExtension.class)
public class StrategyAnalyticsServiceTest {

    @Mock
    private TradeRepository tradeRepository;

    @Mock
    private TradeAnalyticsCalculator analyticsCalculator;

    @InjectMocks
    private StrategyAnalyticsService strategyAnalyticsService;

    @Test
    void shouldGroupTradeByStrategyAndCalculateAnalytics() {

        BrokerAccount brokerAccount = new BrokerAccount();
        brokerAccount.setId(1L);

        Trade breakoutTrade1 = createTrade("BREAKOUT", "1000");

        Trade breakoutTrade2 = createTrade("BREAKOUT", "-400");

        Trade reversalTrade = createTrade("REVERSAL", "500");

        List<Trade> trades = List.of(
                breakoutTrade1,
                breakoutTrade2,
                reversalTrade
        );

        TradeAnalyticsResponse breakoutAnalytics =
                new TradeAnalyticsResponse(
                        2,
                        1,
                        1,
                        new BigDecimal("50.00"),
                        new BigDecimal("600"),
                        new BigDecimal("1000.00"),
                        new BigDecimal("400.00"),
                        new BigDecimal("2.50")
                );

        TradeAnalyticsResponse reversalAnalytics =
                new TradeAnalyticsResponse(
                        1,
                        1,
                        0,
                        new BigDecimal("100.00"),
                        new BigDecimal("500"),
                        new BigDecimal("500.00"),
                        BigDecimal.ZERO,
                        null
                );

        when(tradeRepository.findByBrokerAccount(brokerAccount))
                .thenReturn(trades);

        when(analyticsCalculator.calculate(
                List.of(breakoutTrade1, breakoutTrade2)))
                .thenReturn(breakoutAnalytics);

        when(analyticsCalculator.calculate(
                List.of(reversalTrade)))
                .thenReturn(reversalAnalytics);

        List<StrategyAnalyticsResponse> result = strategyAnalyticsService
                .getAnalytics(brokerAccount);

        assertEquals(2, result.size());

        StrategyAnalyticsResponse breakoutResult = result.stream()
                .filter(response -> response.strategy().equals("BREAKOUT"))
                .findFirst()
                .orElseThrow();

        assertEquals(breakoutAnalytics, breakoutResult.analytics());

        StrategyAnalyticsResponse reversalResult = result.stream()
                .filter(response -> response.strategy().equals("REVERSAL"))
                .findFirst()
                .orElseThrow();

        assertEquals(reversalAnalytics, reversalResult.analytics());

    }

    private Trade createTrade(String strategy, String profitLoss) {

        Decision decision = new Decision();

        decision.setStrategy(strategy);

        Trade trade = new Trade();
        trade.setDecision(decision);
        trade.setProfitLoss(new BigDecimal(profitLoss));

        return trade;

    }

}
