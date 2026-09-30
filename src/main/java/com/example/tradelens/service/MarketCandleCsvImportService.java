package com.example.tradelens.service;



import com.example.tradelens.dto.MarketCandleCsvRow;
import com.example.tradelens.entity.Instrument;
import com.example.tradelens.entity.MarketCandle;
import com.example.tradelens.repository.InstrumentRepository;
import com.example.tradelens.repository.MarketCandleRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import org.apache.commons.csv.CSVRecord;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;

import lombok.RequiredArgsConstructor;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

import java.time.LocalDateTime;
import java.math.BigDecimal;




@Service
@RequiredArgsConstructor
public class MarketCandleCsvImportService {

    private final InstrumentRepository instrumentRepository;

    private final MarketCandleRepository marketCandleRepository;

    @Transactional
    public void importCandles(MultipartFile file) {

        try (
                BufferedReader reader = new BufferedReader(
                        new InputStreamReader(
                                file.getInputStream(),
                                StandardCharsets.UTF_8
                        )
                );

                CSVParser parser = CSVFormat.DEFAULT
                        .builder()
                        .setHeader()
                        .setSkipHeaderRecord(true)
                        .get()
                        .parse(reader)
        ) {
            for(CSVRecord record : parser) {
                MarketCandleCsvRow row = new MarketCandleCsvRow(
                        record.get("symbol"),
                        record.get("exchange"),
                        LocalDateTime.parse(record.get("candle_time")),
                        new BigDecimal(record.get("open")),
                        new BigDecimal(record.get("high")),
                        new BigDecimal(record.get("low")),
                        new BigDecimal(record.get("close")),
                        Long.valueOf(record.get("volume"))
                );

                Instrument instrument = instrumentRepository
                        .findBySymbolAndExchange(
                                row.symbol(),
                                row.exchange()
                        )
                        .orElseThrow(() -> new IllegalArgumentException(
                                "Instrument not found: " + row.symbol() + " on " + row.exchange()
                                )
                        );

                MarketCandle candle = new MarketCandle();

                candle.setInstrument(instrument);
                candle.setCandleTime(row.candleTime());
                candle.setOpen(row.open());
                candle.setHigh(row.high());
                candle.setLow(row.low());
                candle.setClose(row.close());
                candle.setVolume(row.volume());

                marketCandleRepository.save(candle);

            }

        } catch(Exception e) {
            e.printStackTrace();

            throw new IllegalArgumentException(
                    "Failed to import market candle CSV", e
            );
        }

    }

}
