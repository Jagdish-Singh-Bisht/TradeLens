package com.example.tradelens.controller;


import com.example.tradelens.dto.MaeResponse;
import com.example.tradelens.dto.MarketContextResponse;
import com.example.tradelens.dto.MfeResponse;
import com.example.tradelens.dto.PostExitMovementResponse;
import com.example.tradelens.entity.BrokerAccount;
import com.example.tradelens.repository.BrokerAccountRepository;
import com.example.tradelens.repository.TradeRepository;
import com.example.tradelens.service.MarketContextService;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;


@RestController
@RequestMapping ("/api/trades")
@RequiredArgsConstructor
public class MarketContextController {

    private final MarketContextService marketContextService;

    private final BrokerAccountRepository brokerAccountRepository;

    private final TradeRepository tradeRepository;

    @GetMapping("/{tradeId}/mfe")
    public MfeResponse getMfe(@PathVariable Long tradeId,
                              Authentication authentication) {

        verifyTradeOwnership(tradeId, authentication.getName());

        return marketContextService.calculateMfe(tradeId);
    }

    @GetMapping("/{tradeId}/mae")
    public MaeResponse getMae(@PathVariable Long tradeId,
                              Authentication authentication) {

        verifyTradeOwnership(tradeId, authentication.getName());

        return marketContextService.calculateMae(tradeId);
    }

    @GetMapping("/{tradeId}/post-exit-movement")
    public PostExitMovementResponse getPostExitMovement(@PathVariable Long tradeId,
                                                        Authentication authentication) {

        verifyTradeOwnership(tradeId, authentication.getName());

        return marketContextService.calculatePostExitMovement(tradeId);
    }

    @GetMapping("/market-context/{brokerAccountId}")
    public List<MarketContextResponse> getMarketContext(
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

        return marketContextService.getMarketContext(brokerAccount);
    }

    private void verifyTradeOwnership(Long tradeId, String username) {

        tradeRepository
                .findByIdAndBrokerAccount_User_Username(tradeId, username)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Trade not found"
                ));

    }

}
