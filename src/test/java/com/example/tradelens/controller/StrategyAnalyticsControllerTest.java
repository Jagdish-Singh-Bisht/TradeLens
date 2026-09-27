package com.example.tradelens.controller;



import com.example.tradelens.entity.User;
import com.example.tradelens.entity.BrokerAccount;
import com.example.tradelens.entity.Instrument;
import com.example.tradelens.entity.Trade;
import com.example.tradelens.entity.Decision;
import com.example.tradelens.entity.enums.InstrumentType;

import com.example.tradelens.repository.UserRepository;
import com.example.tradelens.repository.BrokerAccountRepository;
import com.example.tradelens.repository.InstrumentRepository;
import com.example.tradelens.repository.DecisionRepository;
import com.example.tradelens.repository.TradeRepository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.util.List;






@SpringBootTest
@AutoConfigureMockMvc
public class StrategyAnalyticsControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BrokerAccountRepository brokerAccountRepository;

    @Autowired
    private InstrumentRepository instrumentRepository;

    @Autowired
    private DecisionRepository  decisionRepository;

    @Autowired
    private TradeRepository tradeRepository;

    @Test
    void shouldReturnStrategyAnalytics() throws Exception {

        User user = new User();
        user.setUsername("strategytest");
        user.setEmail("strategy@test.com");
        user.setPassword("password");
        user = userRepository.save(user);

        BrokerAccount brokerAccount = new BrokerAccount();
        brokerAccount.setBrokerName("TestBroker");
        brokerAccount.setAccountName("Strategy Account");
        brokerAccount.setUser(user);
        brokerAccount = brokerAccountRepository.save(brokerAccount);

        Instrument instrument = new Instrument();
        instrument.setSymbol("TEST_STRATEGY");
        instrument.setExchange("NSE");
        instrument.setType(InstrumentType.EQUITY);
        instrument = instrumentRepository.save(instrument);

        Decision decision1 = new Decision();
        decision1.setStrategy("BREAKOUT");
        decision1.setPlannedEntry(new BigDecimal("100"));
        decision1.setTarget(new BigDecimal("120"));
        decision1.setStopLoss(new BigDecimal("90"));
        decision1.setConfidence(8);
        decision1.setEntryReason("Breakout");

        decision1 = decisionRepository.save(decision1);


        Decision decision2 = new Decision();
        decision2.setStrategy("BREAKOUT");
        decision2.setPlannedEntry(new BigDecimal("100"));
        decision2.setTarget(new BigDecimal("120"));
        decision2.setStopLoss(new BigDecimal("90"));
        decision2.setConfidence(8);
        decision2.setEntryReason("Breakout");

        decision2 = decisionRepository.save(decision2);


        Trade trade1 = createTrade(
                brokerAccount,
                instrument,
                decision1,
                new BigDecimal("100")
        );

        Trade trade2 = createTrade(
                brokerAccount,
                instrument,
                decision2,
                new BigDecimal("-40")
        );

        tradeRepository.saveAll(List.of(trade1, trade2));

        tradeRepository.saveAll(List.of(trade1, trade2));

        mockMvc.perform(
                        get("/api/analytics/strategies/" + brokerAccount.getId())
                                .with(csrf())
                                .with(user("strategytest"))
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].strategy").value("BREAKOUT"))
                .andExpect(jsonPath("$[0].analytics.totalTrades").value(2))
                .andExpect(jsonPath("$[0].analytics.winningTrades").value(1))
                .andExpect(jsonPath("$[0].analytics.losingTrades").value(1))
                .andExpect(jsonPath("$[0].analytics.winRate").value(50.00))
                .andExpect(jsonPath("$[0].analytics.totalProfitLoss").value(60))
                .andExpect(jsonPath("$[0].analytics.averageProfit").value(100))
                .andExpect(jsonPath("$[0].analytics.averageLoss").value(40))
                .andExpect(jsonPath("$[0].analytics.profitFactor").value(2.50));

    }

    private Trade createTrade(
            BrokerAccount brokerAccount,
            Instrument instrument,
            Decision decision,
            BigDecimal profitLoss) {

        Trade trade = new Trade();

        trade.setBrokerAccount(brokerAccount);
        trade.setInstrument(instrument);
        trade.setDecision(decision);
        trade.setQuantity(new BigDecimal("10"));
        trade.setEntryPrice(new BigDecimal("100"));
        trade.setExitPrice(new BigDecimal("110"));
        trade.setProfitLoss(profitLoss);

        return trade;
    }

}
