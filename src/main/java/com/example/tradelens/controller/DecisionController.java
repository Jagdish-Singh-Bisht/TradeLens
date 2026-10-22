package com.example.tradelens.controller;


import com.example.tradelens.repository.TradeRepository;
import com.example.tradelens.service.DecisionService;
import com.example.tradelens.dto.DecisionRequest;
import com.example.tradelens.entity.Decision;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PathVariable;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.RequestBody;

import lombok.RequiredArgsConstructor;
import org.springframework.web.server.ResponseStatusException;


@RestController
@RequestMapping("/api/trades")
@RequiredArgsConstructor
public class DecisionController {

    private final DecisionService decisionService;

    private final TradeRepository tradeRepository;

    @PostMapping("/{tradeId}/decision")
    public void createDecision(@PathVariable Long tradeId,
                               @Valid @RequestBody DecisionRequest request,
                               Authentication authentication) {

        tradeRepository
                .findByIdAndBrokerAccount_User_Username(
                        tradeId,
                        authentication.getName()
                )
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Trade not found"
                ));

        Decision decision = new Decision();

        decision.setStrategy(request.getStrategy());
        decision.setPlannedEntry(request.getPlannedEntry());
        decision.setTarget(request.getTarget());
        decision.setStopLoss(request.getStopLoss());
        decision.setConfidence(request.getConfidence());
        decision.setEntryReason(request.getEntryReason());
        decision.setExitReason(request.getExitReason());

        decisionService.createDecision(
                tradeId,
                authentication.getName(),
                decision
        );

    }




}
