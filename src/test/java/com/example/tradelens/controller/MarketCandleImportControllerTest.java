package com.example.tradelens.controller;


import com.example.tradelens.entity.Instrument;
import com.example.tradelens.entity.MarketCandle;
import com.example.tradelens.entity.enums.InstrumentType;
import com.example.tradelens.repository.InstrumentRepository;
import com.example.tradelens.repository.MarketCandleRepository;

import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;



@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public class MarketCandleImportControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private InstrumentRepository instrumentRepository;

    @Autowired
    private MarketCandleRepository marketCandleRepository;

    @Test
    void shouldImportMarketCandles() throws Exception {

        Instrument instrument = new Instrument();
        instrument.setSymbol("TEST_RELIANCE_001");
        instrument.setExchange("NSE");
        instrument.setType(InstrumentType.EQUITY);

        Instrument savedInstrument = instrumentRepository.save(instrument);

        String csv = """
                symbol,exchange,candle_time,open,high,low,close,volume
                TEST_RELIANCE_001,NSE,2026-09-23T09:30:00,1400,1420,1395,1415,125000
                TEST_RELIANCE_001,NSE,2026-09-23T09:31:00,1415,1425,1410,1420,98000
                """;

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "market-candles.csv",
                MediaType.TEXT_PLAIN_VALUE,
                csv.getBytes()
        );

        mockMvc.perform(
                        multipart("/api/import/market-candles")
                                .file(file)
                                .with(csrf())
                                .with(user("testuser"))
                )
                .andExpect(status().isOk());

        List<MarketCandle> candles =
                marketCandleRepository.findAll()
                        .stream()
                        .filter(candle ->
                                candle.getInstrument().getId().equals(savedInstrument.getId())
                        )
                        .toList();

        assertEquals(2, candles.size());

    }

}
