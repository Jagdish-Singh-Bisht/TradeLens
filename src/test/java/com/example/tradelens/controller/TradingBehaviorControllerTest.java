package com.example.tradelens.controller;


import com.example.tradelens.entity.Decision;
import com.example.tradelens.entity.BrokerAccount;
import com.example.tradelens.entity.Instrument;
import com.example.tradelens.entity.Trade;
import com.example.tradelens.entity.User;
import com.example.tradelens.entity.enums.InstrumentType;
import com.example.tradelens.repository.BrokerAccountRepository;
import com.example.tradelens.repository.DecisionRepository;
import com.example.tradelens.repository.InstrumentRepository;
import com.example.tradelens.repository.TradeRepository;
import com.example.tradelens.repository.UserRepository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

import java.math.BigDecimal;
import java.util.List;





@SpringBootTest
@AutoConfigureMockMvc
public class TradingBehaviorControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TradeRepository tradeRepository;

    @Autowired
    private BrokerAccountRepository brokerAccountRepository;

    @Autowired
    private InstrumentRepository  instrumentRepository;

    @Autowired
    private DecisionRepository  decisionRepository;

    @Test
    void shouldReturnTradingBehavior() throws Exception {

        User user = new User();
        user.setUsername("behaviorTest");
        user.setEmail("behavior@test.com");
        user.setPassword("password");
        user = userRepository.save(user);

        BrokerAccount brokerAccount = new BrokerAccount();
        brokerAccount.setBrokerName("TestBroker");
        brokerAccount.setAccountName("Behavior Account");
        brokerAccount.setUser(user);
        brokerAccount = brokerAccountRepository.save(brokerAccount);

        Instrument instrument = new Instrument();
        instrument.setSymbol("TEST_BEHAVIOR");
        instrument.setExchange("NSE");
        instrument.setType(InstrumentType.EQUITY);
        instrument = instrumentRepository.save(instrument);

        Decision decision1 = createDecision("BREAKOUT");
        Decision decision2 = createDecision("BREAKOUT");
        Decision decision3 = createDecision("REVERSAL");
        Decision decision4 = createDecision("REVERSAL");

        Trade trade1 = createTrade(
                brokerAccount,
                instrument,
                decision1,
                new BigDecimal("115")
        );

        Trade trade2 = createTrade(
                brokerAccount,
                instrument,
                decision2,
                new BigDecimal("120")
        );

        Trade trade3 = createTrade(
                brokerAccount,
                instrument,
                decision3,
                new BigDecimal("120")
        );

        Trade trade4 = createTrade(
                brokerAccount,
                instrument,
                decision4,
                new BigDecimal("90")
        );

        decisionRepository.saveAll(
                List.of(decision1, decision2, decision3, decision4)
        );

        tradeRepository.saveAll(
                List.of(trade1, trade2, trade3, trade4)
        );

        mockMvc.perform(
                        get("/api/analytics/behavior/" + brokerAccount.getId())
                                .with(csrf())
                                .with(user("behaviorTest"))
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalTrades").value(4))
                .andExpect(jsonPath("$.earlyExits").value(2))
                .andExpect(jsonPath("$.lateEntries").value(0))
                .andExpect(jsonPath("$.stopLossHits").value(1))
                .andExpect(jsonPath("$.targetHits").value(2));
    }

    private Decision createDecision(String strategy) {

        Decision decision = new Decision();

        decision.setStrategy(strategy);
        decision.setPlannedEntry(new BigDecimal("100"));
        decision.setTarget(new BigDecimal("120"));
        decision.setStopLoss(new BigDecimal("90"));
        decision.setConfidence(8);
        decision.setEntryReason("Test");

        return decision;
    }

    private Trade createTrade(
            BrokerAccount brokerAccount,
            Instrument instrument,
            Decision decision,
            BigDecimal exitPrice) {

        Trade trade = new Trade();

        trade.setBrokerAccount(brokerAccount);
        trade.setInstrument(instrument);
        trade.setDecision(decision);
        trade.setQuantity(new BigDecimal("10"));
        trade.setEntryPrice(new BigDecimal("100"));
        trade.setExitPrice(exitPrice);
        trade.setProfitLoss(new BigDecimal("100"));

        return trade;
    }


}
