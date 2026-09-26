package com.example.tradelens.service;



import com.example.tradelens.entity.Trade;
import com.example.tradelens.repository.TradeRepository;
import com.example.tradelens.entity.BrokerAccount;
import com.example.tradelens.dto.TradeAnalyticsResponse;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import static org.mockito.Mockito.when;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.util.List;
import java.math.BigDecimal;


@ExtendWith(MockitoExtension.class)
public class TradeAnalyticsServiceTest {

    @Mock
    private TradeRepository tradeRepository;

    @InjectMocks
    private TradeAnalyticsService tradeAnalyticsService;

    @Test
    void shouldCalculateTradeAnalytics() {

        BrokerAccount brokerAccount = new BrokerAccount();
        brokerAccount.setId(1L);

        Trade trade1 = createTrade("1000");
        Trade trade2 = createTrade("500");
        Trade trade3 = createTrade("-400");
        Trade trade4 = createTrade("300");
        Trade trade5 = createTrade("-200");

        when(tradeRepository.findByBrokerAccount(brokerAccount))
                .thenReturn(List.of(
                        trade1,
                        trade2,
                        trade3,
                        trade4,
                        trade5
                ));

        var result = tradeAnalyticsService.getAnalytics(brokerAccount);

        assertEquals(5, result.totalTrades());
        assertEquals(3, result.winningTrades());
        assertEquals(2, result.losingTrades());

        assertEquals(new BigDecimal("60.00"), result.winRate());

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

        BrokerAccount brokerAccount = new BrokerAccount();
        brokerAccount.setId(1L);

        when(tradeRepository.findByBrokerAccount(brokerAccount))
                .thenReturn(List.of());

        TradeAnalyticsResponse result = tradeAnalyticsService
                .getAnalytics(brokerAccount);

        assertEquals(0, result.totalTrades());
        assertEquals(0, result.winningTrades());
        assertEquals(0, result.losingTrades());

        assertEquals(BigDecimal.ZERO, result.winRate());
        assertEquals(BigDecimal.ZERO, result.totalProfitLoss());
        assertEquals(BigDecimal.ZERO, result.averageProfit());
        assertEquals(BigDecimal.ZERO, result.averageLoss());

        assertNull(result.profitFactor());

    }

    @Test
    void shouldHandleOnlyWinningTrades() {

        BrokerAccount brokerAccount = new BrokerAccount();
        brokerAccount.setId(1L);

        Trade trade1 = createTrade("1000");
        Trade trade2 = createTrade("500");
        Trade trade3 = createTrade("300");

        when(tradeRepository.findByBrokerAccount(brokerAccount))
                .thenReturn(List.of(trade1, trade2, trade3));

        TradeAnalyticsResponse result =
                tradeAnalyticsService.getAnalytics(brokerAccount);

        assertEquals(3, result.totalTrades());
        assertEquals(3, result.winningTrades());
        assertEquals(0, result.losingTrades());

        assertEquals(new BigDecimal("100.00"), result.winRate());

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

        BrokerAccount brokerAccount = new BrokerAccount();
        brokerAccount.setId(1L);

        Trade trade1 = createTrade("-400");
        Trade trade2 = createTrade("-200");

        when(tradeRepository.findByBrokerAccount(brokerAccount))
                .thenReturn(List.of(trade1, trade2));

        TradeAnalyticsResponse result =
                tradeAnalyticsService.getAnalytics(brokerAccount);

        assertEquals(2, result.totalTrades());
        assertEquals(0, result.winningTrades());
        assertEquals(2, result.losingTrades());

        assertEquals(new BigDecimal("0.00"), result.winRate());

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
