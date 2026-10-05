package com.example.tradelens.controller;


import com.example.tradelens.entity.BrokerAccount;
import com.example.tradelens.entity.Trade;
import com.example.tradelens.repository.BrokerAccountRepository;
import com.example.tradelens.repository.TradeRepository;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

import java.util.List;

@RestController
@RequestMapping("/api/trades")
@RequiredArgsConstructor
public class TradeController {

    private final TradeRepository tradeRepository;

    private final BrokerAccountRepository brokerAccountRepository;

    @GetMapping("/broker-account/{brokerAccountId}")
    public List<Trade> getTrades(@PathVariable Long brokerAccountId) {

        BrokerAccount brokerAccount = brokerAccountRepository
                .findById(brokerAccountId)
                .orElseThrow(() -> new IllegalArgumentException("Broker account not found"));

        return tradeRepository.findByBrokerAccount(brokerAccount);

    }

    @GetMapping("/{tradeId}")
    public Trade getTrade(@PathVariable Long tradeId) {

        return tradeRepository
                .findById(tradeId)
                .orElseThrow(() -> new IllegalArgumentException("Trade not found"));

    }

}
