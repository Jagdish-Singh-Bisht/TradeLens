package com.example.tradelens.repository;


import com.example.tradelens.entity.User;
import com.example.tradelens.entity.Trade;
import com.example.tradelens.entity.BrokerAccount;
import com.example.tradelens.entity.Instrument;
import com.example.tradelens.entity.Decision;
import com.example.tradelens.entity.enums.InstrumentType;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;




@SpringBootTest
@Transactional
public class DecisionRepositoryTest {

    @Autowired
    private DecisionRepository decisionRepository;

    @Autowired
    private TradeRepository tradeRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BrokerAccountRepository brokerAccountRepository;

    @Autowired
    private InstrumentRepository instrumentRepository;

    @Test
    void shouldAttachDecisionToTrade() {

        User user = new User();
        user.setUsername("testUser");
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

        Decision decision = new Decision();
        decision.setStrategy("BREAKOUT");
        decision.setPlannedEntry(new BigDecimal("1400"));
        decision.setTarget(new BigDecimal("1450"));
        decision.setStopLoss(new BigDecimal("1370"));
        decision.setConfidence(8);
        decision.setEntryReason("Breakout above resistance");
        decision.setExitReason("Target not reached");

        decision = decisionRepository.save(decision);

        Trade trade = new Trade();
        trade.setInstrument(instrument);
        trade.setBrokerAccount(brokerAccount);
        trade.setQuantity(new BigDecimal("100"));
        trade.setEntryPrice(new BigDecimal("1400"));
        trade.setExitPrice(new BigDecimal("1420"));
        trade.setProfitLoss(new BigDecimal("1200"));
        trade.setDecision(decision);

        trade = tradeRepository.save(trade);

        Trade savedTrade = tradeRepository
                .findById(trade.getId())
                .orElseThrow();

        assertNotNull(savedTrade.getDecision());

        assertEquals("BREAKOUT", savedTrade.getDecision().getStrategy());

        assertEquals(
                new BigDecimal("1450"),
                savedTrade.getDecision().getTarget()
        );

    }
}
