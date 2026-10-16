package com.example.tradelens.controller;


import com.example.tradelens.entity.BrokerAccount;
import com.example.tradelens.dto.StrategyAnalyticsResponse;
import com.example.tradelens.repository.BrokerAccountRepository;
import com.example.tradelens.service.StrategyAnalyticsService;

import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.security.core.Authentication;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

import lombok.RequiredArgsConstructor;

import java.util.List;



@RestController
@RequestMapping("/api/analytics")
@RequiredArgsConstructor
public class StrategyAnalyticsController {

    private final StrategyAnalyticsService strategyAnalyticsService;

    private final BrokerAccountRepository  brokerAccountRepository;

    @GetMapping("/strategies/{brokerAccountId}")
    public List<StrategyAnalyticsResponse> getStrategyAnalytics(
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

        return strategyAnalyticsService.getAnalytics(brokerAccount);

    }
}
