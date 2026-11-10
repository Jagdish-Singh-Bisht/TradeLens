package com.example.tradelens.service;


import com.example.tradelens.dto.StrategyAnalyticsResponse;
import com.example.tradelens.entity.BrokerAccount;
import com.example.tradelens.entity.Trade;
import com.example.tradelens.repository.TradeRepository;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;



@Service
@RequiredArgsConstructor
public class StrategyAnalyticsService {

    private final TradeRepository tradeRepository;

    private final TradeAnalyticsCalculator analyticsCalculator;

    public List<StrategyAnalyticsResponse> getAnalytics(BrokerAccount brokerAccount) {
        return getAnalytics(brokerAccount, null, null);
    }

    public List<StrategyAnalyticsResponse> getAnalytics(
            BrokerAccount brokerAccount,
            LocalDate startDate,
            LocalDate endDate
    ) {
        List<Trade> trades = getTrades(brokerAccount, null, startDate, endDate);

        Map<String, List<Trade>> tradesByStrategy =
                trades.stream()
                        .filter(trade -> trade.getDecision() != null)
                        .collect(Collectors.groupingBy(
                                trade -> trade.getDecision().getStrategy()
                        ));

        return tradesByStrategy.entrySet()
                .stream()
                .map(entry -> new StrategyAnalyticsResponse(
                        entry.getKey(),
                        analyticsCalculator.calculate(entry.getValue())
                ))
                .toList();
    }

    public List<StrategyAnalyticsResponse> getConsolidatedAnalytics(String username) {
        return getConsolidatedAnalytics(username, null, null);
    }

    public List<StrategyAnalyticsResponse> getConsolidatedAnalytics(
            String username,
            LocalDate startDate,
            LocalDate endDate
    ) {
        List<Trade> trades = getTrades(null, username, startDate, endDate);

        Map<String, List<Trade>> tradesByStrategy =
                trades.stream()
                        .filter(trade -> trade.getDecision() != null)
                        .collect(Collectors.groupingBy(
                                trade -> trade.getDecision().getStrategy()
                        ));

        return tradesByStrategy.entrySet()
                .stream()
                .map(entry -> new StrategyAnalyticsResponse(
                        entry.getKey(),
                        analyticsCalculator.calculate(entry.getValue())
                ))
                .toList();
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

        if (startDate == null && endDate == null) {
            if (brokerAccount != null) {
                return tradeRepository.findByBrokerAccount(brokerAccount);
            }

            return tradeRepository.findByBrokerAccount_User_Username(username);
        }

        LocalDateTime start = startDate.atStartOfDay();
        LocalDateTime endExclusive = endDate.plusDays(1).atStartOfDay();

        if (brokerAccount != null) {
            return tradeRepository
                    .findByBrokerAccountAndExitTimeGreaterThanEqualAndExitTimeLessThan(
                            brokerAccount, start, endExclusive);
        }

        return tradeRepository
                .findByBrokerAccount_User_UsernameAndExitTimeGreaterThanEqualAndExitTimeLessThan(
                        username, start, endExclusive);
    }

}
