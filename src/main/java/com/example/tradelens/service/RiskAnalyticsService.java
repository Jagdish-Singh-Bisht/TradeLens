package com.example.tradelens.service;


import com.example.tradelens.dto.RiskAnalyticsResponse;
import com.example.tradelens.entity.BrokerAccount;
import com.example.tradelens.entity.Trade;
import com.example.tradelens.repository.BrokerAccountRepository;
import com.example.tradelens.repository.TradeRepository;

import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

import lombok.RequiredArgsConstructor;

import java.time.LocalDate;
import java.util.List;


@Service
@RequiredArgsConstructor
public class RiskAnalyticsService {

    private final RiskAnalyticsCalculator riskAnalyticsCalculator;
    private final TradeRepository tradeRepository;
    private final BrokerAccountRepository brokerAccountRepository;

    public RiskAnalyticsResponse getRiskAnalytics(Long brokerAccountId,
                                                  String username,
                                                  LocalDate startDate,
                                                  LocalDate endDate) {

        if((startDate == null) != (endDate == null)) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Both startDate and endDate must be provided"
            );
        }

        if(startDate != null && startDate.isAfter(endDate)) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "startDate cannot be after endDate"
            );
        }

        List<Trade> trades;

        if(brokerAccountId == null) {

            if(startDate == null) {

                trades = tradeRepository
                        .findByBrokerAccount_User_Username(username);

            } else {

                trades = tradeRepository
                        .findByBrokerAccount_User_UsernameAndExitTimeGreaterThanEqualAndExitTimeLessThan(
                                username,
                                startDate.atStartOfDay(),
                                endDate.plusDays(1).atStartOfDay()
                        );
            }

        } else {

            BrokerAccount brokerAccount = brokerAccountRepository
                    .findByIdAndUser_Username(brokerAccountId, username)
                    .orElseThrow(() -> new ResponseStatusException(
                            HttpStatus.NOT_FOUND,
                            "Broker account not found"
                    ));

            if(startDate == null) {

                trades = tradeRepository
                        .findByBrokerAccount(brokerAccount);

            } else {

                trades = tradeRepository
                        .findByBrokerAccountAndExitTimeGreaterThanEqualAndExitTimeLessThan(
                                brokerAccount,
                                startDate.atStartOfDay(),
                                endDate.plusDays(1).atStartOfDay()
                        );
            }
        }

        return riskAnalyticsCalculator.calculate(trades);

    }



}
