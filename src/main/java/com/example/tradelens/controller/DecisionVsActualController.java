package com.example.tradelens.controller;


import com.example.tradelens.dto.DecisionVsActualResponse;
import com.example.tradelens.service.DecisionVsActualService;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;



@RestController
@RequestMapping("/api/trades")
@RequiredArgsConstructor
public class DecisionVsActualController {

    private final DecisionVsActualService decisionVsActualService;

    @GetMapping("/{tradeId}/decision-analysis")
    public DecisionVsActualResponse analyze(@PathVariable Long tradeId) {

        return decisionVsActualService.analyze(tradeId);

    }


}
