package com.example.tradelens.controller;


import com.example.tradelens.dto.PnlTrendResponse;
import com.example.tradelens.dto.TradeAnalyticsResponse;
import com.example.tradelens.entity.BrokerAccount;
import com.example.tradelens.repository.BrokerAccountRepository;
import com.example.tradelens.service.TradeAnalyticsService;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.security.core.Authentication;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;


import lombok.RequiredArgsConstructor;

import java.util.List;


@RestController
@RequestMapping("/api/broker-accounts")
@RequiredArgsConstructor
public class TradeAnalyticsController {

    private final TradeAnalyticsService tradeAnalyticsService;

    private final BrokerAccountRepository brokerAccountRepository;

    @GetMapping("/{brokerAccountId}/analytics")
    public TradeAnalyticsResponse getAnalytics(
            @PathVariable Long brokerAccountId,
            Authentication authentication) {

        BrokerAccount brokerAccount = brokerAccountRepository
                .findByIdAndUser_Username(
                        brokerAccountId,
                        authentication.getName()
                )
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Broker account not found"
                ));

        return tradeAnalyticsService.getAnalytics(brokerAccount);

    }

    @GetMapping("/{brokerAccountId}/analytics/pnl-trend")
    public List<PnlTrendResponse> getPnlTrend(
            @PathVariable Long brokerAccountId,
            Authentication authentication) {

        BrokerAccount brokerAccount = brokerAccountRepository
                .findByIdAndUser_Username(
                        brokerAccountId,
                        authentication.getName()
                )
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Broker account not found"
                ));

        return tradeAnalyticsService.getPnlTrend(brokerAccount);

    }


}
