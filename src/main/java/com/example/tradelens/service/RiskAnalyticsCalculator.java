package com.example.tradelens.service;


import com.example.tradelens.dto.RiskAnalyticsResponse;
import com.example.tradelens.entity.Trade;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.List;
import java.math.BigDecimal;
import java.math.RoundingMode;


@Component
public class RiskAnalyticsCalculator {

    public BigDecimal calculateExpectancy(List<Trade> trades) {

        if(trades.isEmpty()) {
            return BigDecimal.ZERO;
        }

        BigDecimal netProfitLoss = trades.stream()
                .map(Trade::getProfitLoss)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return netProfitLoss.divide(
                BigDecimal.valueOf(trades.size()),
                2,
                RoundingMode.HALF_UP
        );

    }

    public BigDecimal calculateMaximumDrawdown(List<Trade> trades) {

        List<Trade> orderedTrades = trades.stream()
                .sorted(
                        Comparator.comparing(Trade::getExitTime)
                                .thenComparing(Trade::getId)
                )
                .toList();

        BigDecimal cumulativeProfitLoss = BigDecimal.ZERO;
        BigDecimal peak = BigDecimal.ZERO;
        BigDecimal maximumDrawdown = BigDecimal.ZERO;

        for(Trade trade : orderedTrades) {

            cumulativeProfitLoss = cumulativeProfitLoss.add(
                    trade.getProfitLoss()
            );

            if(cumulativeProfitLoss.compareTo(peak) > 0) {
                peak = cumulativeProfitLoss;
            }

            BigDecimal currentDrawdown = peak.subtract(
                    cumulativeProfitLoss
            );

            if(currentDrawdown.compareTo(maximumDrawdown) > 0) {
                maximumDrawdown = currentDrawdown;
            }

        }

        return maximumDrawdown;
    }

    public long calculateMaximumConsecutiveWins(List<Trade> trades) {

        long currentStreak  = 0;
        long maximumStreak = 0;

        for(Trade trade : trades) {

            if(trade.getProfitLoss().compareTo(BigDecimal.ZERO) > 0) {

                currentStreak++;

                maximumStreak = Math.max(
                        currentStreak,
                        maximumStreak
                );
            } else {
                currentStreak = 0;
            }
        }

        return maximumStreak;
    }

    public long calculateMaximumConsecutiveLosses(List<Trade> trades) {

        long currentStreak  = 0;
        long maximumStreak = 0;

        for(Trade trade : trades) {

            if(trade.getProfitLoss().compareTo(BigDecimal.ZERO) < 0) {
                currentStreak++;

                maximumStreak = Math.max(
                        currentStreak,
                        maximumStreak
                );

            } else {
                currentStreak = 0;
            }
        }

        return maximumStreak;
    }

    public BigDecimal calculateRecoveryFactor(
            BigDecimal netProfitLoss,
            BigDecimal maximumDrawdown) {

        if(maximumDrawdown.compareTo(BigDecimal.ZERO) == 0) {
            return null;
        }

        return netProfitLoss.divide(
                maximumDrawdown,
                2,
                RoundingMode.HALF_UP
        );

    }

    public RiskAnalyticsResponse calculate(List<Trade> trades) {

        List<Trade> orderedTrades = trades.stream()
                .sorted(
                        Comparator.comparing(Trade::getExitTime)
                                .thenComparing(Trade::getId)
                )
                .toList();

        BigDecimal netProfitLoss = trades.stream()
                .map(Trade::getProfitLoss)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal expectancy = calculateExpectancy(trades);

        BigDecimal maximumDrawdown =
                calculateMaximumDrawdown(orderedTrades);

        long maximumConsecutiveWins =
                calculateMaximumConsecutiveWins(orderedTrades);

        long maximumConsecutiveLosses =
                calculateMaximumConsecutiveLosses(orderedTrades);

        BigDecimal recoveryFactor =
                calculateRecoveryFactor(netProfitLoss, maximumDrawdown);

        return new RiskAnalyticsResponse(
                trades.size(),
                netProfitLoss,
                expectancy,
                maximumDrawdown,
                maximumConsecutiveWins,
                maximumConsecutiveLosses,
                recoveryFactor
        );
    }

}
