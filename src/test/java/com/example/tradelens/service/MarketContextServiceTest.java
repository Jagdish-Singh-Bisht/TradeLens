package com.example.tradelens.service;


import com.example.tradelens.dto.PostExitMovementResponse;
import com.example.tradelens.dto.MaeResponse;
import com.example.tradelens.dto.MfeResponse;
import com.example.tradelens.entity.MarketCandle;
import com.example.tradelens.entity.Instrument;
import com.example.tradelens.entity.Trade;
import com.example.tradelens.entity.enums.InstrumentType;
import com.example.tradelens.repository.MarketCandleRepository;
import com.example.tradelens.repository.TradeRepository;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;




@ExtendWith(MockitoExtension.class)
public class MarketContextServiceTest {

    @Mock
    private TradeRepository tradeRepository;

    @Mock
    private MarketCandleRepository marketCandleRepository;

    @InjectMocks
    private MarketContextService marketContextService;

    @Test
    void shouldReturnCandlesForTradeTimeRange() {

        Instrument instrument = new Instrument();
        instrument.setId(1L);
        instrument.setSymbol("TEST_RELIANCE");
        instrument.setExchange("NSE");
        instrument.setType(InstrumentType.EQUITY);

        Trade trade = new Trade();
        trade.setId(1L);
        trade.setInstrument(instrument);
        trade.setEntryTime(
                LocalDateTime.of(2026, 9, 23, 9, 30)
        );
        trade.setExitTime(
                LocalDateTime.of(2026, 9, 23, 9, 32)
        );

        MarketCandle candle1 = new MarketCandle();
        candle1.setInstrument(instrument);
        candle1.setCandleTime(
                LocalDateTime.of(2026, 9, 23, 9, 30)
        );

        MarketCandle candle2 = new MarketCandle();
        candle2.setInstrument(instrument);
        candle2.setCandleTime(
                LocalDateTime.of(2026, 9, 23, 9, 31)
        );

        MarketCandle candle3 = new MarketCandle();
        candle3.setInstrument(instrument);
        candle3.setCandleTime(
                LocalDateTime.of(2026, 9, 23, 9, 32)
        );

        List<MarketCandle> candles =
                List.of(candle1, candle2, candle3);

        when(tradeRepository.findById(1L))
                .thenReturn(Optional.of(trade));

        when(marketCandleRepository
                .findByInstrumentAndCandleTimeBetweenOrderByCandleTimeAsc(
                        instrument,
                        trade.getEntryTime(),
                        trade.getExitTime()
                ))
                .thenReturn(candles);

        List<MarketCandle> result =
                marketContextService.getCandlesForTrade(1L);

        assertEquals(3, result.size());
        assertEquals(candle1, result.get(0));
        assertEquals(candle2, result.get(1));
        assertEquals(candle3, result.get(2));

    }

    @Test
    void shouldCalculateMfe() {

        Instrument instrument = new Instrument();
        instrument.setId(1L);
        instrument.setSymbol("TEST_RELIANCE");
        instrument.setExchange("NSE");
        instrument.setType(InstrumentType.EQUITY);

        Trade trade = new Trade();
        trade.setId(1L);
        trade.setInstrument(instrument);
        trade.setEntryPrice(new BigDecimal("100"));
        trade.setEntryTime(
                LocalDateTime.of(2026, 9, 23, 9, 30)
        );
        trade.setExitTime(
                LocalDateTime.of(2026, 9, 23, 9, 32)
        );

        MarketCandle candle1 = new MarketCandle();
        candle1.setHigh(new BigDecimal("105"));

        MarketCandle candle2 = new MarketCandle();
        candle2.setHigh(new BigDecimal("108"));

        MarketCandle candle3 = new MarketCandle();
        candle3.setHigh(new BigDecimal("110"));

        List<MarketCandle> candles =
                List.of(candle1, candle2, candle3);

        when(tradeRepository.findById(1L))
                .thenReturn(Optional.of(trade));

        when(marketCandleRepository
                .findByInstrumentAndCandleTimeBetweenOrderByCandleTimeAsc(
                        instrument,
                        trade.getEntryTime(),
                        trade.getExitTime()
                ))
                .thenReturn(candles);

        MfeResponse result =
                marketContextService.calculateMfe(1L);

        assertEquals(
                new BigDecimal("100"),
                result.entryPrice()
        );

        assertEquals(
                new BigDecimal("110"),
                result.highestPrice()
        );

        assertEquals(
                new BigDecimal("10"),
                result.mfe()
        );
    }

    @Test
    void shouldThrowExceptionWhenNoCandlesFound() {

        Instrument instrument = new Instrument();
        instrument.setId(1L);

        Trade trade = new Trade();
        trade.setId(1L);
        trade.setInstrument(instrument);
        trade.setEntryPrice(new BigDecimal("100"));
        trade.setEntryTime(
                LocalDateTime.of(2026, 9, 23, 9, 30)
        );
        trade.setExitTime(
                LocalDateTime.of(2026, 9, 23, 9, 32)
        );

        when(tradeRepository.findById(1L))
                .thenReturn(Optional.of(trade));

        when(marketCandleRepository
                .findByInstrumentAndCandleTimeBetweenOrderByCandleTimeAsc(
                        instrument,
                        trade.getEntryTime(),
                        trade.getExitTime()
                ))
                .thenReturn(List.of());

        IllegalArgumentException exception =
                org.junit.jupiter.api.Assertions.assertThrows(
                        IllegalArgumentException.class,
                        () -> marketContextService.calculateMfe(1L)
                );

        assertEquals(
                "No market candles found for trade",
                exception.getMessage()
        );

    }

    @Test
    void shouldCalculateMae() {

        Instrument instrument = new Instrument();
        instrument.setId(1L);
        instrument.setSymbol("TEST_RELIANCE");
        instrument.setExchange("NSE");
        instrument.setType(InstrumentType.EQUITY);

        Trade trade = new Trade();
        trade.setId(1L);
        trade.setInstrument(instrument);
        trade.setEntryPrice(new BigDecimal("100"));
        trade.setEntryTime(
                LocalDateTime.of(2026, 9, 23, 9, 30)
        );
        trade.setExitTime(
                LocalDateTime.of(2026, 9, 23, 9, 32)
        );

        MarketCandle candle1 = new MarketCandle();
        candle1.setLow(new BigDecimal("98"));

        MarketCandle candle2 = new MarketCandle();
        candle2.setLow(new BigDecimal("96"));

        MarketCandle candle3 = new MarketCandle();
        candle3.setLow(new BigDecimal("99"));

        List<MarketCandle> candles =
                List.of(candle1, candle2, candle3);

        when(tradeRepository.findById(1L))
                .thenReturn(Optional.of(trade));

        when(marketCandleRepository
                .findByInstrumentAndCandleTimeBetweenOrderByCandleTimeAsc(
                        instrument,
                        trade.getEntryTime(),
                        trade.getExitTime()
                ))
                .thenReturn(candles);

        MaeResponse result =
                marketContextService.calculateMae(1L);

        assertEquals(
                new BigDecimal("100"),
                result.entryPrice()
        );

        assertEquals(
                new BigDecimal("96"),
                result.lowestPrice()
        );

        assertEquals(
                new BigDecimal("4"),
                result.mae()
        );

    }

    @Test
    void shouldCalculatePostExitMovement() {

        Instrument instrument = new Instrument();
        instrument.setId(1L);
        instrument.setSymbol("TEST_RELIANCE");
        instrument.setExchange("NSE");
        instrument.setType(InstrumentType.EQUITY);

        Trade trade = new Trade();
        trade.setId(1L);
        trade.setInstrument(instrument);
        trade.setEntryPrice(new BigDecimal("100"));
        trade.setExitPrice(new BigDecimal("107"));
        trade.setEntryTime(
                LocalDateTime.of(2026, 9, 23, 9, 30)
        );
        trade.setExitTime(
                LocalDateTime.of(2026, 9, 23, 9, 32)
        );

        MarketCandle candle1 = new MarketCandle();
        candle1.setHigh(new BigDecimal("108"));

        MarketCandle candle2 = new MarketCandle();
        candle2.setHigh(new BigDecimal("111"));

        MarketCandle candle3 = new MarketCandle();
        candle3.setHigh(new BigDecimal("115"));

        List<MarketCandle> candles =
                List.of(candle1, candle2, candle3);

        when(tradeRepository.findById(1L))
                .thenReturn(Optional.of(trade));

        when(marketCandleRepository
                .findByInstrumentAndCandleTimeGreaterThanOrderByCandleTimeAsc(
                        instrument,
                        trade.getExitTime()
                ))
                .thenReturn(candles);

        PostExitMovementResponse result =
                marketContextService.calculatePostExitMovement(1L);

        assertEquals(
                new BigDecimal("107"),
                result.exitPrice()
        );

        assertEquals(
                new BigDecimal("115"),
                result.highestPriceAfterExit()
        );

        assertEquals(
                new BigDecimal("8"),
                result.postExitMovement()
        );

    }

    @Test
    void shouldThrowExceptionWhenNoCandlesFoundAfterExit() {

        Instrument instrument = new Instrument();
        instrument.setId(1L);

        Trade trade = new Trade();
        trade.setId(1L);
        trade.setInstrument(instrument);
        trade.setEntryPrice(new BigDecimal("100"));
        trade.setExitPrice(new BigDecimal("107"));

        trade.setEntryTime(
                LocalDateTime.of(2026, 9, 23, 9, 30)
        );

        trade.setExitTime(
                LocalDateTime.of(2026, 9, 23, 9, 32)
        );

        when(tradeRepository.findById(1L))
                .thenReturn(Optional.of(trade));

        when(marketCandleRepository
                .findByInstrumentAndCandleTimeGreaterThanOrderByCandleTimeAsc(
                        instrument,
                        trade.getExitTime()
                ))
                .thenReturn(List.of());

        IllegalArgumentException exception =
                Assertions.assertThrows(
                        IllegalArgumentException.class,
                        () -> marketContextService.calculatePostExitMovement(1L)
                );

        assertEquals(
                "No market candles found after trade exit",
                exception.getMessage()
        );

    }

}