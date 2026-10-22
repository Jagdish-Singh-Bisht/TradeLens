package com.example.tradelens.controller;



import com.example.tradelens.dto.TradeResponse;
import com.example.tradelens.entity.Decision;
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
    public List<TradeResponse> getTrades(
            @PathVariable Long brokerAccountId,
            Authentication authentication) {

        return tradeRepository
                .findByBrokerAccount_IdAndBrokerAccount_User_Username(
                        brokerAccountId,
                        authentication.getName()
                )
                .stream()
                .map(this::toTradeResponse)
                .toList();

    }

    @GetMapping("/{tradeId}")
    public TradeResponse getTrade(@PathVariable Long tradeId,
                          Authentication authentication) {

        Trade trade = tradeRepository
                .findByIdAndBrokerAccount_User_Username(
                        tradeId,
                        authentication.getName()
                )
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Trade not found"
                ));

        return toTradeResponse(trade);

    }

    public TradeResponse toTradeResponse(Trade trade) {

        Decision decision = trade.getDecision();

        TradeResponse.DecisionResponse decisionResponse = decision == null
                ? null
                : new TradeResponse.DecisionResponse(
                decision.getStrategy(),
                decision.getPlannedEntry(),
                decision.getTarget(),
                decision.getStopLoss(),
                decision.getConfidence(),
                decision.getEntryReason(),
                decision.getExitReason()
        );

        return new TradeResponse(
                trade.getId(),
                trade.getInstrument() != null
                        ? trade.getInstrument().getSymbol() : null,
                trade.getInstrument() != null
                        ? trade.getInstrument().getExchange() : null,
                trade.getQuantity(),
                trade.getEntryPrice(),
                trade.getExitPrice(),
                trade.getEntryTime(),
                trade.getExitTime(),
                trade.getProfitLoss(),
                decisionResponse

        );
    }

}
