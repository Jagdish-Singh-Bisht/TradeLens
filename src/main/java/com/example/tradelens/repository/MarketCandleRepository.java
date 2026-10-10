package com.example.tradelens.repository;

import com.example.tradelens.entity.Instrument;
import com.example.tradelens.entity.MarketCandle;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;


public interface MarketCandleRepository extends JpaRepository<MarketCandle, Long> {

    List<MarketCandle> findByInstrumentAndCandleTimeBetweenOrderByCandleTimeAsc(
            Instrument instrument,
            LocalDateTime startTime,
            LocalDateTime endTime
    );

    List<MarketCandle> findByInstrumentAndCandleTimeGreaterThanOrderByCandleTimeAsc(
            Instrument instrument,
            LocalDateTime exitTime
    );

    List<MarketCandle> findByInstrumentOrderByCandleTimeAsc(
            Instrument instrument
    );

}