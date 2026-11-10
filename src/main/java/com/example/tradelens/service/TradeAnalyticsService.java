package com.example.tradelens.service;


import com.example.tradelens.dto.PnlTrendResponse;
import com.example.tradelens.entity.Trade;
import com.example.tradelens.entity.BrokerAccount;
import com.example.tradelens.dto.TradeAnalyticsResponse;
import com.example.tradelens.repository.TradeRepository;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
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

       return getAnalytics(brokerAccount, null, null);
    }

    public TradeAnalyticsResponse getAnalytics(BrokerAccount brokerAccount,
                                               LocalDate startDate,
                                               LocalDate endDate ) {

        List<Trade> trades = getTrades(brokerAccount, null, startDate, endDate);
        return analyticsCalculator.calculate(trades);
    }


    public TradeAnalyticsResponse getConsolidatedAnalytics(String username) {

        return getConsolidatedAnalytics(username, null, null);
    }

    public TradeAnalyticsResponse getConsolidatedAnalytics(String username,
                                                           LocalDate startDate,
                                                           LocalDate endDate) {

        List<Trade> trades = getTrades(null, username, startDate, endDate);

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

    private List<PnlTrendResponse> buildPnlTrend(List<Trade> trades) {

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

    public List<PnlTrendResponse> getPnlTrend(BrokerAccount brokerAccount) {

        return getPnlTrend(brokerAccount, null, null);
    }

    public List<PnlTrendResponse> getPnlTrend(BrokerAccount brokerAccount,
                                              LocalDate startDate,
                                              LocalDate endDate) {

        List<Trade> trades = getTrades(brokerAccount, null, startDate, endDate);

        return buildPnlTrend(trades);
    }

    public List<PnlTrendResponse> getConsolidatedPnlTrend(String username) {

        return getConsolidatedPnlTrend(username, null, null);
    }

    public List<PnlTrendResponse> getConsolidatedPnlTrend(String username,
                                                          LocalDate startDate,
                                                          LocalDate endDate) {

        List<Trade> trades = getTrades(null, username, startDate, endDate);

        return buildPnlTrend(trades);
    }

    private List<Trade> getTrades(BrokerAccount brokerAccount,
                                  String username,
                                  LocalDate startDate,
                                  LocalDate endDate) {

        if ((startDate == null) != (endDate == null)) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Both startDate and endDate must be provided"
            );
        }

        if (startDate != null && startDate.isAfter(endDate)) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "startDate cannot be after endDate"
            );
        }

        if(startDate == null && endDate == null) {
            if(brokerAccount != null) {
                return tradeRepository.findByBrokerAccount(brokerAccount);
            }

            return tradeRepository.findByBrokerAccount_User_Username(username);
        }

        LocalDateTime start = startDate.atStartOfDay();
        LocalDateTime endExclusive = endDate.plusDays(1).atStartOfDay();

        if(brokerAccount != null) {
            return tradeRepository
                    .findByBrokerAccountAndExitTimeGreaterThanEqualAndExitTimeLessThan(
                            brokerAccount, start, endExclusive
                    );
        }

        return tradeRepository
                .findByBrokerAccount_User_UsernameAndExitTimeGreaterThanEqualAndExitTimeLessThan(
                        username, start, endExclusive
                );
    }



}
