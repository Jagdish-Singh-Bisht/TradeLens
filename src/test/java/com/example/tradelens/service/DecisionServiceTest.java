package com.example.tradelens.service;


import com.example.tradelens.entity.Decision;
import com.example.tradelens.entity.BrokerAccount;
import com.example.tradelens.entity.Instrument;
import com.example.tradelens.entity.Trade;
import com.example.tradelens.entity.User;
import com.example.tradelens.entity.enums.InstrumentType;
import com.example.tradelens.repository.BrokerAccountRepository;
import com.example.tradelens.repository.InstrumentRepository;
import com.example.tradelens.repository.TradeRepository;
import com.example.tradelens.repository.UserRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;


import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;


@SpringBootTest
@Transactional
class DecisionServiceTest {

    @Autowired
    private DecisionService decisionService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BrokerAccountRepository brokerAccountRepository;

    @Autowired
    private InstrumentRepository instrumentRepository;

    @Autowired
    private TradeRepository tradeRepository;

    @Test
    void shouldCreateAndAttachDecisionToTrade() {

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

        Decision decision = new Decision();

        decision.setStrategy("BREAKOUT");
        decision.setPlannedEntry(new BigDecimal("1400"));
        decision.setTarget(new BigDecimal("1450"));
        decision.setStopLoss(new BigDecimal("1370"));
        decision.setConfidence(8);
        decision.setEntryReason("Breakout above resistance");
        decision.setExitReason("Took profit early");

        decisionService.createDecision(trade.getId(), decision);

        Trade savedTrade = tradeRepository.findById(trade.getId())
                .orElseThrow();

        assertNotNull(savedTrade.getDecision());

        assertEquals(
                "BREAKOUT",
                savedTrade.getDecision().getStrategy()
        );

        assertEquals(
                new BigDecimal("1450"),
                savedTrade.getDecision().getTarget()
        );

    }

    @Test
    void shouldThrowExceptionWhenTradeDoesNotExist() {

        Decision decision = new Decision();

        decision.setStrategy("BREAKOUT");
        decision.setPlannedEntry(new BigDecimal("1400"));
        decision.setTarget(new BigDecimal("1450"));
        decision.setStopLoss(new BigDecimal("1370"));
        decision.setConfidence(8);

        assertThrows(
                IllegalArgumentException.class,
                () -> decisionService.createDecision(999999L, decision)
        );
    }
}
