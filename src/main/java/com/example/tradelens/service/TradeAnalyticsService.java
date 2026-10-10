package com.example.tradelens.service;


import com.example.tradelens.dto.PnlTrendResponse;
import com.example.tradelens.entity.Trade;
import com.example.tradelens.entity.BrokerAccount;
import com.example.tradelens.dto.TradeAnalyticsResponse;
import com.example.tradelens.repository.TradeRepository;

import org.springframework.stereotype.Service;

import java.math.RoundingMode;
import java.util.List;
import java.math.BigDecimal;
import java.util.ArrayList;



@Service
public class TradeAnalyticsService {

    private final TradeRepository tradeRepository;

    private final TradeAnalyticsCalculator analyticsCalculator;

    public TradeAnalyticsService(TradeRepository tradeRepository,
                                 TradeAnalyticsCalculator analyticsCalculator) {
        this.tradeRepository = tradeRepository;
        this.analyticsCalculator = analyticsCalculator;
    }

    public TradeAnalyticsResponse getAnalytics(BrokerAccount brokerAccount) {

       List<Trade> trades = tradeRepository.findByBrokerAccount(brokerAccount);

       return analyticsCalculator.calculate(trades);

    }

    private BigDecimal calculateWinRate(long winningTrades, long totalTrades) {

        if(totalTrades == 0){
            return BigDecimal.ZERO;
        }

        return BigDecimal.valueOf(winningTrades)
                .multiply(BigDecimal.valueOf(100))
                .divide(
                        BigDecimal.valueOf(totalTrades),
                        2,
                        RoundingMode.HALF_UP
                );

    }

    private BigDecimal calculateAverage(BigDecimal total, long count) {

        if(count == 0) {
            return BigDecimal.ZERO;
        }

        return total.divide(
                BigDecimal.valueOf(count),
                2,
                RoundingMode.HALF_UP
        );

    }

    private BigDecimal calculateProfitFactor(BigDecimal grossProfit,
                                             BigDecimal grossLoss ) {

        if(grossLoss.compareTo(BigDecimal.ZERO) == 0) {
            return null;
        }

        return grossProfit.divide(
                grossLoss.abs(),
                2,
                RoundingMode.HALF_UP
        );

    }

    public List<PnlTrendResponse> getPnlTrend(BrokerAccount brokerAccount) {

        List<Trade> trades = tradeRepository
                .findByBrokerAccount(brokerAccount);

        trades.sort((a, b) -> a.getExitTime().compareTo(b.getExitTime()));

        List<PnlTrendResponse> result = new ArrayList<>();

        BigDecimal cumulativeProfitLoss = BigDecimal.ZERO;

        int tradeNumber = 1;

        for(Trade trade : trades) {

            cumulativeProfitLoss = cumulativeProfitLoss
                    .add(trade.getProfitLoss());

            result.add(
                    new PnlTrendResponse(
                            tradeNumber,
                            trade.getProfitLoss(),
                            cumulativeProfitLoss
                    )
            );

            tradeNumber++;

        }

        return result;

    }



}
