package com.example.tradelens.service;


import com.example.tradelens.dto.MaeResponse;
import com.example.tradelens.dto.MarketContextResponse;
import com.example.tradelens.dto.MfeResponse;
import com.example.tradelens.dto.PostExitMovementResponse;
import com.example.tradelens.entity.BrokerAccount;
import com.example.tradelens.entity.MarketCandle;
import com.example.tradelens.entity.Trade;
import com.example.tradelens.repository.MarketCandleRepository;
import com.example.tradelens.repository.TradeRepository;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

import java.util.List;
import java.math.BigDecimal;


@Service
@RequiredArgsConstructor
public class MarketContextService {

    private final TradeRepository tradeRepository;

    private final MarketCandleRepository marketCandleRepository;

    public List<MarketCandle> getCandlesForTrade(Long tradeId) {

        Trade trade = tradeRepository
                .findById(tradeId)
                .orElseThrow(() -> new IllegalArgumentException("Trade not found"));

        return marketCandleRepository.findByInstrumentAndCandleTimeBetweenOrderByCandleTimeAsc(
                trade.getInstrument(),
                trade.getEntryTime(),
                trade.getExitTime()
        );

    }

    public MfeResponse calculateMfe(Long tradeId) {

        Trade trade = tradeRepository
                .findById(tradeId)
                .orElseThrow(() -> new IllegalArgumentException("Trade not found"));

        List<MarketCandle> candles = marketCandleRepository
                .findByInstrumentAndCandleTimeBetweenOrderByCandleTimeAsc(
                        trade.getInstrument(),
                        trade.getEntryTime(),
                        trade.getExitTime()
                );

        if (candles.isEmpty()) {
            throw new IllegalArgumentException("No market candles found for trade");
        }

        BigDecimal highestPrice = candles.stream()
                .map(MarketCandle::getHigh)
                .max(BigDecimal::compareTo)
                .orElseThrow();

        BigDecimal mfe = highestPrice
                .subtract(trade.getEntryPrice());

        return new MfeResponse(
                trade.getEntryPrice(),
                highestPrice,
                mfe
        );

    }

    public MaeResponse calculateMae(Long tradeId) {

        Trade trade = tradeRepository.findById(tradeId)
                .orElseThrow(() ->
                        new IllegalArgumentException("Trade not found"));

        List<MarketCandle> candles =
                marketCandleRepository
                        .findByInstrumentAndCandleTimeBetweenOrderByCandleTimeAsc(
                                trade.getInstrument(),
                                trade.getEntryTime(),
                                trade.getExitTime()
                        );

        if (candles.isEmpty()) {
            throw new IllegalArgumentException(
                    "No market candles found for trade"
            );
        }

        BigDecimal lowestPrice = candles.stream()
                .map(MarketCandle::getLow)
                .min(BigDecimal::compareTo)
                .orElseThrow();

        BigDecimal mae = trade.getEntryPrice()
                .subtract(lowestPrice);

        return new MaeResponse(
                trade.getEntryPrice(),
                lowestPrice,
                mae
        );

    }

    public PostExitMovementResponse calculatePostExitMovement(Long tradeId) {

        Trade trade = tradeRepository.findById(tradeId)
                .orElseThrow(() ->
                        new IllegalArgumentException("Trade not found"));

        List<MarketCandle> candles =
                marketCandleRepository
                        .findByInstrumentAndCandleTimeGreaterThanOrderByCandleTimeAsc(
                                trade.getInstrument(),
                                trade.getExitTime()
                        );

        if (candles.isEmpty()) {
            throw new IllegalArgumentException(
                    "No market candles found after trade exit"
            );
        }

        BigDecimal highestPriceAfterExit = candles.stream()
                .map(MarketCandle::getHigh)
                .max(BigDecimal::compareTo)
                .orElseThrow();

        BigDecimal postExitMovement =
                highestPriceAfterExit.subtract(trade.getExitPrice());

        return new PostExitMovementResponse(
                trade.getExitPrice(),
                highestPriceAfterExit,
                postExitMovement
        );

    }

    public List<MarketContextResponse> getMarketContext(BrokerAccount brokerAccount) {

        List<Trade> trades = tradeRepository.findByBrokerAccount(brokerAccount);

        return trades.stream()
                .map(trade -> {

                    MfeResponse mfe = null;
                    MaeResponse mae = null;
                    PostExitMovementResponse postExit = null;

                    try {
                        mfe = calculateMfe(trade.getId());
                    } catch (IllegalArgumentException e) {
                        System.out.println(
                                "MFE unavailable for trade " + trade.getId()
                        );
                    }

                    try {
                        mae = calculateMae(trade.getId());
                    } catch (IllegalArgumentException e) {
                        System.out.println(
                                "MAE unavailable for trade " + trade.getId()
                        );
                    }

                    try {
                        postExit = calculatePostExitMovement(trade.getId());
                    } catch (IllegalArgumentException e) {
                        System.out.println(
                                "Post-exit movement unavailable for trade "
                                        + trade.getId()
                        );
                    }

                    return new MarketContextResponse(
                            trade.getId(),
                            trade.getInstrument().getSymbol(),
                            trade.getEntryPrice(),
                            trade.getExitPrice(),
                            mfe != null ? mfe.mfe() : null,
                            mae != null ? mae.mae() : null,
                            postExit != null ? postExit.postExitMovement() : null
                    );
                })
                .toList();
    }


}
