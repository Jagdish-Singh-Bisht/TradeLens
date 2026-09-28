package com.example.tradelens.controller;


import com.example.tradelens.dto.TradingBehaviorResponse;
import com.example.tradelens.entity.BrokerAccount;
import com.example.tradelens.repository.BrokerAccountRepository;
import com.example.tradelens.service.TradingBehaviorService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/analytics")
@RequiredArgsConstructor
public class TradingBehaviorController {

    private final TradingBehaviorService tradingBehaviorService;

    private final BrokerAccountRepository brokerAccountRepository;

    @GetMapping("/behavior/{brokerAccountId}")
    public TradingBehaviorResponse getBehavior(@PathVariable Long brokerAccountId) {

        BrokerAccount brokerAccount = brokerAccountRepository
                .findById(brokerAccountId)
                .orElseThrow(() -> new IllegalArgumentException("Broker Account not found"));

        return tradingBehaviorService.getBehavior(brokerAccount);

    }

}
