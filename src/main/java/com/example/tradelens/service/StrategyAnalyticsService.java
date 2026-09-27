package com.example.tradelens.service;


import com.example.tradelens.dto.StrategyAnalyticsResponse;
import com.example.tradelens.entity.BrokerAccount;
import com.example.tradelens.entity.Trade;
import com.example.tradelens.repository.TradeRepository;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;



@Service
@RequiredArgsConstructor
public class StrategyAnalyticsService {

    private final TradeRepository tradeRepository;

    private final TradeAnalyticsCalculator analyticsCalculator;

    public List<StrategyAnalyticsResponse> getAnalytics(BrokerAccount brokerAccount) {

        List<Trade> trades = tradeRepository
                .findByBrokerAccount(brokerAccount);

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


}
