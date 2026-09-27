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

import java.util.List;
import java.math.BigDecimal;


@ExtendWith(MockitoExtension.class)
public class TradeAnalyticsServiceTest {

    @Mock
    private TradeRepository tradeRepository;

    @Mock
    private TradeAnalyticsCalculator analyticsCalculator;

    @InjectMocks
    private TradeAnalyticsService tradeAnalyticsService;

    @Test
    void shouldGetAnalyticsUsingCalculator() {

        BrokerAccount brokerAccount = new BrokerAccount();
        brokerAccount.setId(1L);

        Trade trade1 = createTrade("1000");
        Trade trade2 = createTrade("-400");

        List<Trade> trades = List.of(trade1, trade2);

        TradeAnalyticsResponse expectedResponse =
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

        when(tradeRepository.findByBrokerAccount(brokerAccount))
                .thenReturn(trades);

        when(analyticsCalculator.calculate(trades))
                .thenReturn(expectedResponse);

        TradeAnalyticsResponse result =
                tradeAnalyticsService.getAnalytics(brokerAccount);

        assertEquals(expectedResponse, result);
    }


    private Trade createTrade(String profitLoss) {

        Trade trade = new Trade();
        trade.setProfitLoss(new BigDecimal(profitLoss));

        return trade;
    }


}
