package com.example.tradelens.controller;


import com.example.tradelens.dto.InstrumentOption;
import com.example.tradelens.repository.InstrumentRepository;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

import java.util.List;


@RestController
@RequestMapping("/api/instruments")
@RequiredArgsConstructor
public class InstrumentController {

    private final InstrumentRepository instrumentRepository;

    @GetMapping
    public List<InstrumentOption> getInstruments() {

        return instrumentRepository.findAll()
                .stream()
                .map(instrument -> new InstrumentOption(
                        instrument.getSymbol(),
                        instrument.getExchange()
                ))
                .toList();

    }




}
