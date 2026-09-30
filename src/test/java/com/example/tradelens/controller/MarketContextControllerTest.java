package com.example.tradelens.controller;



import com.example.tradelens.entity.enums.InstrumentType;
import com.example.tradelens.entity.MarketCandle;
import com.example.tradelens.entity.Trade;
import com.example.tradelens.entity.Instrument;
import com.example.tradelens.repository.MarketCandleRepository;
import com.example.tradelens.repository.InstrumentRepository;
import com.example.tradelens.repository.TradeRepository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

import java.util.List;
import java.math.BigDecimal;
import java.time.LocalDateTime;





@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public class MarketContextControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private TradeRepository tradeRepository;

    @Autowired
    private InstrumentRepository instrumentRepository;

    @Autowired
    private MarketCandleRepository marketCandleRepository;

    @Test
    void shouldReturnMfeForTrade() throws Exception {

        Instrument instrument = new Instrument();
        instrument.setSymbol("TEST_MFE_CONTROLLER");
        instrument.setExchange("NSE");
        instrument.setType(InstrumentType.EQUITY);

        instrument = instrumentRepository.save(instrument);

        Trade trade = new Trade();
        trade.setInstrument(instrument);
        trade.setEntryPrice(new BigDecimal("100"));
        trade.setExitPrice(new BigDecimal("107"));
        trade.setEntryTime(
                LocalDateTime.of(2026, 9, 30, 9, 30)
        );
        trade.setExitTime(
                LocalDateTime.of(2026, 9, 30, 9, 32)
        );

        trade = tradeRepository.save(trade);

        MarketCandle candle1 = new MarketCandle();
        candle1.setInstrument(instrument);
        candle1.setCandleTime(
                LocalDateTime.of(2026, 9, 30, 9, 30)
        );
        candle1.setHigh(new BigDecimal("105"));

        MarketCandle candle2 = new MarketCandle();
        candle2.setInstrument(instrument);
        candle2.setCandleTime(
                LocalDateTime.of(2026, 9, 30, 9, 31)
        );
        candle2.setHigh(new BigDecimal("108"));

        MarketCandle candle3 = new MarketCandle();
        candle3.setInstrument(instrument);
        candle3.setCandleTime(
                LocalDateTime.of(2026, 9, 30, 9, 32)
        );
        candle3.setHigh(new BigDecimal("110"));

        marketCandleRepository.saveAll(
                List.of(candle1, candle2, candle3)
        );

        mockMvc.perform(
                        get("/api/trades/{tradeId}/mfe", trade.getId())
                                .with(csrf())
                                .with(user("testuser"))
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.entryPrice").value(100))
                .andExpect(jsonPath("$.highestPrice").value(110))
                .andExpect(jsonPath("$.mfe").value(10));

    }

    @Test
    void shouldReturnMaeForTrade() throws Exception {

        Instrument instrument = new Instrument();
        instrument.setSymbol("TEST_MAE_CONTROLLER");
        instrument.setExchange("NSE");
        instrument.setType(InstrumentType.EQUITY);

        instrument = instrumentRepository.save(instrument);

        Trade trade = new Trade();
        trade.setInstrument(instrument);
        trade.setEntryPrice(new BigDecimal("100"));
        trade.setExitPrice(new BigDecimal("107"));
        trade.setEntryTime(
                LocalDateTime.of(2026, 9, 30, 9, 30)
        );
        trade.setExitTime(
                LocalDateTime.of(2026, 9, 30, 9, 32)
        );

        trade = tradeRepository.save(trade);

        MarketCandle candle1 = new MarketCandle();
        candle1.setInstrument(instrument);
        candle1.setCandleTime(
                LocalDateTime.of(2026, 9, 30, 9, 30)
        );
        candle1.setLow(new BigDecimal("98"));

        MarketCandle candle2 = new MarketCandle();
        candle2.setInstrument(instrument);
        candle2.setCandleTime(
                LocalDateTime.of(2026, 9, 30, 9, 31)
        );
        candle2.setLow(new BigDecimal("96"));

        MarketCandle candle3 = new MarketCandle();
        candle3.setInstrument(instrument);
        candle3.setCandleTime(
                LocalDateTime.of(2026, 9, 30, 9, 32)
        );
        candle3.setLow(new BigDecimal("99"));

        marketCandleRepository.saveAll(
                List.of(candle1, candle2, candle3)
        );

        mockMvc.perform(
                        get("/api/trades/{tradeId}/mae", trade.getId())
                                .with(csrf())
                                .with(user("testuser"))
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.entryPrice").value(100))
                .andExpect(jsonPath("$.lowestPrice").value(96))
                .andExpect(jsonPath("$.mae").value(4));

    }

    @Test
    void shouldReturnPostExitMovementForTrade() throws Exception {

        Instrument instrument = new Instrument();
        instrument.setSymbol("TEST_POST_EXIT_CONTROLLER");
        instrument.setExchange("NSE");
        instrument.setType(InstrumentType.EQUITY);

        instrument = instrumentRepository.save(instrument);

        Trade trade = new Trade();
        trade.setInstrument(instrument);
        trade.setEntryPrice(new BigDecimal("100"));
        trade.setExitPrice(new BigDecimal("107"));
        trade.setEntryTime(
                LocalDateTime.of(2026, 9, 30, 9, 30)
        );
        trade.setExitTime(
                LocalDateTime.of(2026, 9, 30, 9, 32)
        );

        trade = tradeRepository.save(trade);

        MarketCandle candle1 = new MarketCandle();
        candle1.setInstrument(instrument);
        candle1.setCandleTime(
                LocalDateTime.of(2026, 9, 30, 9, 33)
        );
        candle1.setHigh(new BigDecimal("108"));

        MarketCandle candle2 = new MarketCandle();
        candle2.setInstrument(instrument);
        candle2.setCandleTime(
                LocalDateTime.of(2026, 9, 30, 9, 34)
        );
        candle2.setHigh(new BigDecimal("111"));

        MarketCandle candle3 = new MarketCandle();
        candle3.setInstrument(instrument);
        candle3.setCandleTime(
                LocalDateTime.of(2026, 9, 30, 9, 35)
        );
        candle3.setHigh(new BigDecimal("115"));

        marketCandleRepository.saveAll(
                List.of(candle1, candle2, candle3)
        );

        mockMvc.perform(
                        get(
                                "/api/trades/{tradeId}/post-exit-movement",
                                trade.getId()
                        )
                                .with(csrf())
                                .with(user("testuser"))
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.exitPrice").value(107))
                .andExpect(jsonPath("$.highestPriceAfterExit").value(115))
                .andExpect(jsonPath("$.postExitMovement").value(8));

    }

}