package com.example.tradelens.service;

import com.example.tradelens.dto.TradeAnalyticsResponse;
import com.example.tradelens.entity.Trade;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class TradeAnalyticsCalculatorTest {

    private final TradeAnalyticsCalculator calculator =
            new TradeAnalyticsCalculator();

    @Test
    void shouldCalculateTradeAnalytics() {

        Trade trade1 = createTrade("1000");
        Trade trade2 = createTrade("500");
        Trade trade3 = createTrade("-400");
        Trade trade4 = createTrade("300");
        Trade trade5 = createTrade("-200");

        TradeAnalyticsResponse result =
                calculator.calculate(List.of(
                        trade1,
                        trade2,
                        trade3,
                        trade4,
                        trade5
                ));

        assertEquals(5, result.totalTrades());
        assertEquals(3, result.winningTrades());
        assertEquals(2, result.losingTrades());

        assertEquals(
                new BigDecimal("60.00"),
                result.winRate()
        );

        assertEquals(
                new BigDecimal("1200"),
                result.totalProfitLoss()
        );

        assertEquals(
                new BigDecimal("600.00"),
                result.averageProfit()
        );

        assertEquals(
                new BigDecimal("300.00"),
                result.averageLoss()
        );

        assertEquals(
                new BigDecimal("3.00"),
                result.profitFactor()
        );
    }

    @Test
    void shouldReturnZeroAnalyticsWhenThereAreNoTrades() {

        TradeAnalyticsResponse result =
                calculator.calculate(List.of());

        assertEquals(0, result.totalTrades());
        assertEquals(0, result.winningTrades());
        assertEquals(0, result.losingTrades());

        assertEquals(
                BigDecimal.ZERO,
                result.winRate()
        );

        assertEquals(
                BigDecimal.ZERO,
                result.totalProfitLoss()
        );

        assertEquals(
                BigDecimal.ZERO,
                result.averageProfit()
        );

        assertEquals(
                BigDecimal.ZERO,
                result.averageLoss()
        );

        assertNull(result.profitFactor());
    }

    @Test
    void shouldHandleOnlyWinningTrades() {

        Trade trade1 = createTrade("1000");
        Trade trade2 = createTrade("500");
        Trade trade3 = createTrade("300");

        TradeAnalyticsResponse result =
                calculator.calculate(List.of(
                        trade1,
                        trade2,
                        trade3
                ));

        assertEquals(3, result.totalTrades());
        assertEquals(3, result.winningTrades());
        assertEquals(0, result.losingTrades());

        assertEquals(
                new BigDecimal("100.00"),
                result.winRate()
        );

        assertEquals(
                new BigDecimal("1800"),
                result.totalProfitLoss()
        );

        assertEquals(
                new BigDecimal("600.00"),
                result.averageProfit()
        );

        assertEquals(
                BigDecimal.ZERO,
                result.averageLoss()
        );

        assertNull(result.profitFactor());
    }

    @Test
    void shouldHandleOnlyLosingTrades() {

        Trade trade1 = createTrade("-400");
        Trade trade2 = createTrade("-200");

        TradeAnalyticsResponse result =
                calculator.calculate(List.of(
                        trade1,
                        trade2
                ));

        assertEquals(2, result.totalTrades());
        assertEquals(0, result.winningTrades());
        assertEquals(2, result.losingTrades());

        assertEquals(
                new BigDecimal("0.00"),
                result.winRate()
        );

        assertEquals(
                new BigDecimal("-600"),
                result.totalProfitLoss()
        );

        assertEquals(
                BigDecimal.ZERO,
                result.averageProfit()
        );

        assertEquals(
                new BigDecimal("300.00"),
                result.averageLoss()
        );

        assertEquals(
                new BigDecimal("0.00"),
                result.profitFactor()
        );
    }

    private Trade createTrade(String profitLoss) {

        Trade trade = new Trade();
        trade.setProfitLoss(new BigDecimal(profitLoss));

        return trade;
    }
}