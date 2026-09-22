package com.example.tradelens.controller;


import com.example.tradelens.service.DecisionService;
import com.example.tradelens.dto.DecisionRequest;
import com.example.tradelens.entity.Decision;

import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PathVariable;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.RequestBody;

import lombok.RequiredArgsConstructor;




@RestController
@RequestMapping("/api/trades")
@RequiredArgsConstructor
public class DecisionController {

    private final DecisionService decisionService;

    @PostMapping("/{tradeId}/decision")
    public void createDecision(@PathVariable Long tradeId,
                               @Valid @RequestBody DecisionRequest request) {

        Decision decision = new Decision();

        decision.setStrategy(request.getStrategy());
        decision.setPlannedEntry(request.getPlannedEntry());
        decision.setTarget(request.getTarget());
        decision.setStopLoss(request.getStopLoss());
        decision.setConfidence(request.getConfidence());
        decision.setEntryReason(request.getEntryReason());
        decision.setExitReason(request.getExitReason());

        decisionService.createDecision(tradeId, decision);

    }




}
