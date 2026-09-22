package com.example.tradelens.controller;



import com.example.tradelens.entity.User;
import com.example.tradelens.entity.BrokerAccount;
import com.example.tradelens.entity.Instrument;
import com.example.tradelens.entity.Trade;
import com.example.tradelens.entity.enums.InstrumentType;
import com.example.tradelens.repository.UserRepository;
import com.example.tradelens.repository.BrokerAccountRepository;
import com.example.tradelens.repository.InstrumentRepository;
import com.example.tradelens.repository.TradeRepository;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;

import org.springframework.http.MediaType;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;

import java.math.BigDecimal;



@SpringBootTest
@Transactional
@AutoConfigureMockMvc
public class DecisionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BrokerAccountRepository brokerAccountRepository;

    @Autowired
    private InstrumentRepository instrumentRepository;

    @Autowired
    private TradeRepository tradeRepository;

    @Test
    void shouldCreateDecisionThroughApi() throws Exception {

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

        Trade trade = new Trade();
        trade.setInstrument(instrument);
        trade.setBrokerAccount(brokerAccount);
        trade.setQuantity(new BigDecimal("100"));
        trade.setEntryPrice(new BigDecimal("1400"));
        trade.setExitPrice(new BigDecimal("1420"));
        trade.setProfitLoss(new BigDecimal("2000"));

        trade = tradeRepository.save(trade);

        String requestBody = """
               {
                   "strategy": "BREAKOUT",
                   "plannedEntry": 1400.00,
                   "target": 1450.00,
                   "stopLoss": 1370.00,
                   "confidence": 8,
                   "entryReason": "Breakout above resistance",
                   "exitReason": "Took profit early"
               
               }
               """;

        mockMvc.perform(
                post("/api/trades/" + trade.getId() + "/decision")
                        .with(csrf())
                        .with(user("testuser"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody)
        )
                .andExpect(status().isOk());

        Trade savedTrade = tradeRepository
                .findById(trade.getId())
                .orElseThrow();

        assertNotNull(savedTrade.getDecision());

        assertEquals("BREAKOUT", savedTrade.getDecision().getStrategy());

        assertEquals(new BigDecimal("1450.00"), savedTrade.getDecision().getTarget());

    }

    @Test
    void shouldRejectInvalidConfidence() throws Exception {

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

        Trade trade = new Trade();
        trade.setInstrument(instrument);
        trade.setBrokerAccount(brokerAccount);
        trade.setQuantity(new BigDecimal("100"));
        trade.setEntryPrice(new BigDecimal("1400"));
        trade.setExitPrice(new BigDecimal("1420"));
        trade.setProfitLoss(new BigDecimal("2000"));

        trade = tradeRepository.save(trade);

        String requestBody = """
        {
            "strategy": "BREAKOUT",
            "plannedEntry": 1400,
            "target": 1450,
            "stopLoss": 1370,
            "confidence": 15,
            "entryReason": "Breakout above resistance",
            "exitReason": "Took profit early"
        }
        """;

        mockMvc.perform(
                        post("/api/trades/" + trade.getId() + "/decision")
                                .with(csrf())
                                .with(user("testuser"))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(requestBody)
                )
                .andExpect(status().isBadRequest());
    }

}
