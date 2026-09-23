package com.example.tradelens.repository;


import com.example.tradelens.entity.Instrument;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;


public interface InstrumentRepository extends JpaRepository<Instrument, Long> {

    Optional<Instrument> findBySymbolAndExchange(
            String symbol,
            String exchange
    );


}
