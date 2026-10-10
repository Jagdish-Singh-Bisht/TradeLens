package com.example.tradelens.controller;



import com.example.tradelens.dto.MarketCandleCsvRow;
import com.example.tradelens.entity.Instrument;
import com.example.tradelens.entity.MarketCandle;
import com.example.tradelens.repository.InstrumentRepository;
import com.example.tradelens.repository.MarketCandleRepository;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.GetMapping;

import lombok.RequiredArgsConstructor;

import java.util.List;


@RestController
@RequestMapping("/api/market-candles")
@RequiredArgsConstructor
public class MarketCandleController {

    private final InstrumentRepository instrumentRepository;

    private final MarketCandleRepository marketCandleRepository;

    @GetMapping("/{symbol}/{exchange}")
    public List<MarketCandleCsvRow> getCandles(@PathVariable String symbol,
                                               @PathVariable String exchange) {

        Instrument instrument = instrumentRepository
                .findBySymbolAndExchange(symbol, exchange)
                .orElseThrow(() -> new IllegalArgumentException("Instrument not found"));

        List<MarketCandle> candles = marketCandleRepository
                .findByInstrumentOrderByCandleTimeAsc(instrument);

        return candles.stream()
                .map(candle -> new MarketCandleCsvRow(
                        candle.getInstrument().getSymbol(),
                        candle.getInstrument().getExchange(),
                        candle.getCandleTime(),
                        candle.getOpen(),
                        candle.getHigh(),
                        candle.getLow(),
                        candle.getClose(),
                        candle.getVolume()
                ))
                .toList();

    }

}
