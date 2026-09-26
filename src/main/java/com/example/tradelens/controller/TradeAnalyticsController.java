package com.example.tradelens.controller;


import com.example.tradelens.dto.TradeAnalyticsResponse;
import com.example.tradelens.entity.BrokerAccount;
import com.example.tradelens.repository.BrokerAccountRepository;
import com.example.tradelens.service.TradeAnalyticsService;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;






@RestController
@RequestMapping("/api/broker-accounts")
@RequiredArgsConstructor
public class TradeAnalyticsController {

    private final TradeAnalyticsService tradeAnalyticsService;

    private final BrokerAccountRepository brokerAccountRepository;

    @GetMapping("/{brokerAccountId}/analytics")
    public TradeAnalyticsResponse getAnalytics(@PathVariable Long brokerAccountId) {

        BrokerAccount brokerAccount = brokerAccountRepository
                .findById(brokerAccountId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Broker account not found"
                ));

        return tradeAnalyticsService.getAnalytics(brokerAccount);

    }


}
