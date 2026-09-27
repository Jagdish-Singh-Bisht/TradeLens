package com.example.tradelens.service;

import com.example.tradelens.dto.TradeAnalyticsResponse;
import com.example.tradelens.entity.Trade;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Component
public class TradeAnalyticsCalculator {

    public TradeAnalyticsResponse calculate(List<Trade> trades) {

        long totalTrades = trades.size();

        long winningTrades = trades.stream()
                .filter(trade -> trade.getProfitLoss()
                        .compareTo(BigDecimal.ZERO) > 0)
                .count();

        long losingTrades = trades.stream()
                .filter(trade -> trade.getProfitLoss()
                        .compareTo(BigDecimal.ZERO) < 0)
                .count();

        BigDecimal totalProfitLoss = trades.stream()
                .map(Trade::getProfitLoss)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal grossProfit = trades.stream()
                .map(Trade::getProfitLoss)
                .filter(profitLoss -> profitLoss.compareTo(BigDecimal.ZERO) > 0)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal grossLoss = trades.stream()
                .map(Trade::getProfitLoss)
                .filter(profitLoss -> profitLoss.compareTo(BigDecimal.ZERO) < 0)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal winRate = calculateWinRate(
                winningTrades,
                totalTrades
        );

        BigDecimal averageProfit = calculateAverage(
                grossProfit,
                winningTrades
        );

        BigDecimal averageLoss = calculateAverage(
                grossLoss.abs(),
                losingTrades
        );

        BigDecimal profitFactor = calculateProfitFactor(
                grossProfit,
                grossLoss
        );

        return new TradeAnalyticsResponse(
                totalTrades,
                winningTrades,
                losingTrades,
                winRate,
                totalProfitLoss,
                averageProfit,
                averageLoss,
                profitFactor
        );
    }

    private BigDecimal calculateWinRate(
            long winningTrades,
            long totalTrades) {

        if (totalTrades == 0) {
            return BigDecimal.ZERO;
        }

        return BigDecimal.valueOf(winningTrades)
                .multiply(BigDecimal.valueOf(100))
                .divide(
                        BigDecimal.valueOf(totalTrades),
                        2,
                        RoundingMode.HALF_UP
                );
    }

    private BigDecimal calculateAverage(
            BigDecimal total,
            long count) {

        if (count == 0) {
            return BigDecimal.ZERO;
        }

        return total.divide(
                BigDecimal.valueOf(count),
                2,
                RoundingMode.HALF_UP
        );
    }

    private BigDecimal calculateProfitFactor(
            BigDecimal grossProfit,
            BigDecimal grossLoss) {

        if (grossLoss.compareTo(BigDecimal.ZERO) == 0) {
            return null;
        }

        return grossProfit.divide(
                grossLoss.abs(),
                2,
                RoundingMode.HALF_UP
        );
    }
}