package com.example.tradelens.controller;



import com.example.tradelens.entity.Trade;
import com.example.tradelens.repository.TradeRepository;

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
@RequestMapping("/api/trades")
@RequiredArgsConstructor
public class TradeController {

    private final TradeRepository tradeRepository;

    @GetMapping("/broker-account/{brokerAccountId}")
    public List<Trade> getTrades(
            @PathVariable Long brokerAccountId,
            Authentication authentication) {

        return tradeRepository
                .findByBrokerAccount_IdAndBrokerAccount_User_Username(
                        brokerAccountId,
                        authentication.getName()
                );

    }

    @GetMapping("/{tradeId}")
    public Trade getTrade(@PathVariable Long tradeId,
                          Authentication authentication) {

        return tradeRepository
                .findByIdAndBrokerAccount_User_Username(
                        tradeId,
                        authentication.getName()
                )
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Trade not found"
                ));

    }

}
