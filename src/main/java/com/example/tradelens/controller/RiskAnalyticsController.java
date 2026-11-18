package com.example.tradelens.controller;



import com.example.tradelens.dto.RiskAnalyticsResponse;
import com.example.tradelens.service.RiskAnalyticsService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.format.annotation.DateTimeFormat;

import org.springframework.security.core.Authentication;

import lombok.RequiredArgsConstructor;

import java.time.LocalDate;




@RestController
@RequestMapping("/api/risk-analytics")
@RequiredArgsConstructor
public class RiskAnalyticsController {

    private final RiskAnalyticsService riskAnalyticsService;

    @GetMapping
    public RiskAnalyticsResponse getRiskAnalytics(@RequestParam(required = false) Long brokerAccountId,

                                                  @RequestParam(required = false)
                                                  @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
                                                  LocalDate startDate,

                                                  @RequestParam(required = false)
                                                  @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
                                                  LocalDate endDate,

                                                  Authentication authentication) {

        return riskAnalyticsService.getRiskAnalytics(
                brokerAccountId,
                authentication.getName(),
                startDate,
                endDate
        );

    }

}
