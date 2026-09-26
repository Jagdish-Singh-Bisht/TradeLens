package com.example.tradelens.controller;



import com.example.tradelens.entity.Trade;
import com.example.tradelens.entity.Execution;
import com.example.tradelens.entity.Instrument;
import com.example.tradelens.entity.User;
import com.example.tradelens.entity.BrokerAccount;
import com.example.tradelens.entity.enums.InstrumentType;
import com.example.tradelens.repository.*;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import java.math.BigDecimal;


@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public class CsvImportControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BrokerAccountRepository brokerAccountRepository;

    @Autowired
    private InstrumentRepository instrumentRepository;

    @Autowired
    private ExecutionRepository executionRepository;

    @Autowired
    private TradeRepository tradeRepository;

    @Test
    void shouldAcceptCsvFile() throws Exception {

        User user = new User();
        user.setUsername("testuser");
        user.setEmail("test@example.com");
        user.setPassword("password");

        user = userRepository.save(user);

        BrokerAccount brokerAccount = new BrokerAccount();
        brokerAccount.setBrokerName("TestBroker");
        brokerAccount.setAccountName("Test Account");
        brokerAccount.setUser(user);

        brokerAccount = brokerAccountRepository.save(brokerAccount);

        Instrument instrument = new Instrument();
        instrument.setSymbol("TEST_RELIANCE");
        instrument.setExchange("NSE");
        instrument.setType(InstrumentType.EQUITY);

        instrument = instrumentRepository.save(instrument);



        MockMultipartFile file = new MockMultipartFile(
                "file",
                "executions.csv",
                "text/csv",
                """
                order_id,symbol,exchange,side,quantity,price,executed_at
                ORD001,TEST_RELIANCE,NSE,BUY,30,1400.00,2026-09-23T09:30:00
                ORD002,TEST_RELIANCE,NSE,BUY,40,1402.00,2026-09-23T09:31:00
                ORD003,TEST_RELIANCE,NSE,SELL,50,1430.00,2026-09-23T10:15:00
                """.getBytes()
        );

        mockMvc.perform(
                multipart("/api/import/executions")
                        .file(file)
                        .param(
                                "brokerAccountId",
                                brokerAccount.getId().toString()
                        )
                        .with(csrf())
                        .with(user("testuser"))
        )
                .andExpect(status().isOk());

        List<Execution> executions = executionRepository
                .findAll();

        assertEquals(3, executions.size());

        List<Trade> trades = tradeRepository.findByBrokerAccount(brokerAccount);

        assertEquals(1, trades.size());

        Trade trade = trades.get(0);

        assertEquals(new BigDecimal("50"), trade.getQuantity());
        assertEquals(new BigDecimal("1400.80"), trade.getEntryPrice());
        assertEquals(new BigDecimal("1430.00"), trade.getExitPrice());
        assertEquals(new BigDecimal("1460.00"), trade.getProfitLoss());

    }


    @Test
    void shouldRejectInvalidCsv() throws Exception {

        User user = new User();
        user.setUsername("testuser");
        user.setEmail("test@example.com");
        user.setPassword("password");
        user = userRepository.save(user);

        BrokerAccount brokerAccount = new BrokerAccount();
        brokerAccount.setBrokerName("TestBroker");
        brokerAccount.setAccountName("Test Account");
        brokerAccount.setUser(user);
        brokerAccount = brokerAccountRepository.save(brokerAccount);

        Instrument instrument = new Instrument();
        instrument.setSymbol("RELIANCE");
        instrument.setExchange("NSE");
        instrument.setType(InstrumentType.EQUITY);
        instrument = instrumentRepository.save(instrument);

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "executions.csv",
                "text/csv",
                """
                order_id,symbol,exchange,side,quantity,price,executed_at
                ORD002,RELIANCE,NSE,BUY,abc,1400.00,2026-09-23T09:30:00
                """.getBytes()
        );

        mockMvc.perform(
                multipart("/api/import/executions")
                        .file(file)
                        .param("brokerAccountId", brokerAccount.getId().toString())
                        .with(csrf())
                        .with(user("testuser"))
        ).andExpect(status().isBadRequest());

    }
}
