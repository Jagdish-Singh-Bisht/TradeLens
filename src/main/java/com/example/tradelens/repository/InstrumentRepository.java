package com.example.tradelens.repository;


import com.example.tradelens.entity.Instrument;
import org.springframework.data.jpa.repository.JpaRepository;


public interface InstrumentRepository extends JpaRepository<Instrument, Long> {


}
