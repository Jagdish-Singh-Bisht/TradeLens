package com.example.tradelens.service;


import com.example.tradelens.entity.BrokerAccount;
import com.example.tradelens.entity.Instrument;
import com.example.tradelens.entity.Trade;
import com.example.tradelens.entity.User;
import com.example.tradelens.entity.enums.InstrumentType;
import com.example.tradelens.repository.BrokerAccountRepository;
import com.example.tradelens.repository.InstrumentRepository;
import com.example.tradelens.repository.TradeRepository;
import com.example.tradelens.repository.UserRepository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;

import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;


@SpringBootTest
public class CsvImportServiceTest {

    @Autowired
    private CsvImportService csvImportService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BrokerAccountRepository brokerAccountRepository;

    @Autowired
    private InstrumentRepository instrumentRepository;

    @Autowired
    private TradeRepository tradeRepository;

    @Test
    void shouldSkipDuplicateExecutionsWhenSameCsvIsImportedTwice() {

        User user = new User();
        user.setUsername("csv-import-test");
        user.setEmail("csv-import@test.com");
        user.setPassword("test123");

        user = userRepository.save(user);

        BrokerAccount brokerAccount = new BrokerAccount();
        brokerAccount.setBrokerName("Test Broker");
        brokerAccount.setAccountName("CSV Test Account");
        brokerAccount.setUser(user);

        brokerAccount = brokerAccountRepository.save(brokerAccount);

        Instrument instrument = instrumentRepository
                .findBySymbolAndExchange("RELIANCE", "NSE")
                .orElseGet(() -> {
                    Instrument newInstrument = new Instrument();
                    newInstrument.setSymbol("RELIANCE");
                    newInstrument.setExchange("NSE");
                    newInstrument.setType(InstrumentType.EQUITY);
                    return instrumentRepository.save(newInstrument);
                });

        String csv = """
                order_id,symbol,exchange,side,quantity,price,executed_at
                CSV-TEST-BUY,RELIANCE,NSE,BUY,10,1400.00,2026-09-23T09:30:00
                CSV-TEST-SELL,RELIANCE,NSE,SELL,10,1450.00,2026-09-23T10:00:00
                """;

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "test.csv",
                "text/csv",
                csv.getBytes(StandardCharsets.UTF_8)
        );

        csvImportService.importExecutions(
                brokerAccount.getId(),
                file
        );

        List<Trade> tradesAfterFirstImport =
                tradeRepository.findByBrokerAccount(brokerAccount);

        assertEquals(1, tradesAfterFirstImport.size());

        csvImportService.importExecutions(
                brokerAccount.getId(),
                file
        );

        List<Trade> tradesAfterSecondImport =
                tradeRepository.findByBrokerAccount(brokerAccount);

        assertEquals(1, tradesAfterSecondImport.size());

    }

}
