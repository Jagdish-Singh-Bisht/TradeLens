package com.example.tradelens.controller;


import com.example.tradelens.dto.MaeResponse;
import com.example.tradelens.dto.MarketContextResponse;
import com.example.tradelens.dto.MfeResponse;
import com.example.tradelens.dto.PostExitMovementResponse;
import com.example.tradelens.entity.BrokerAccount;
import com.example.tradelens.repository.BrokerAccountRepository;
import com.example.tradelens.service.MarketContextService;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

import java.util.List;


@RestController
@RequestMapping ("/api/trades")
@RequiredArgsConstructor
public class MarketContextController {

    private final MarketContextService marketContextService;

    private final BrokerAccountRepository brokerAccountRepository;

    @GetMapping("/{tradeId}/mfe")
    public MfeResponse getMfe(@PathVariable Long tradeId) {

        return marketContextService.calculateMfe(tradeId);
    }

    @GetMapping("/{tradeId}/mae")
    public MaeResponse getMae(@PathVariable Long tradeId) {

        return marketContextService.calculateMae(tradeId);
    }

    @GetMapping("/{tradeId}/post-exit-movement")
    public PostExitMovementResponse getPostExitMovement(@PathVariable Long tradeId) {

        return marketContextService.calculatePostExitMovement(tradeId);
    }

    @GetMapping("/market-context/{brokerAccountId}")
    public List<MarketContextResponse> getMarketContext(
            @PathVariable Long brokerAccountId) {

        BrokerAccount brokerAccount = brokerAccountRepository
                .findById(brokerAccountId)
                .orElseThrow(() -> new IllegalArgumentException("Broker account not found"));

        return marketContextService.getMarketContext(brokerAccount);
    }

}
