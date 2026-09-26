package com.example.tradelens.controller;

import com.example.tradelens.entity.Decision;
import com.example.tradelens.entity.Trade;
import com.example.tradelens.repository.DecisionRepository;
import com.example.tradelens.repository.TradeRepository;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;




@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class DecisionVsActualControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private TradeRepository tradeRepository;

    @Autowired
    private DecisionRepository decisionRepository;

    @Test
    void shouldReturnDecisionVsActualAnalysis() throws Exception {

        Decision decision = new Decision();
        decision.setStrategy("BREAKOUT");
        decision.setPlannedEntry(new BigDecimal("1400.00"));
        decision.setTarget(new BigDecimal("1450.00"));
        decision.setStopLoss(new BigDecimal("1380.00"));
        decision.setConfidence(8);
        decision.setEntryReason("Breakout above resistance");

        decision = decisionRepository.save(decision);

        Trade trade = new Trade();
        trade.setEntryPrice(new BigDecimal("1405.00"));
        trade.setExitPrice(new BigDecimal("1430.00"));
        trade.setProfitLoss(new BigDecimal("1250.00"));
        trade.setDecision(decision);

        trade = tradeRepository.save(trade);

        mockMvc.perform(
                        get("/api/trades/" + trade.getId() + "/decision-analysis")
                                .with(csrf())
                                .with(user("testuser"))
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.plannedEntry").value(1400.00))
                .andExpect(jsonPath("$.actualEntry").value(1405.00))
                .andExpect(jsonPath("$.entryDeviation").value(5.00))
                .andExpect(jsonPath("$.target").value(1450.00))
                .andExpect(jsonPath("$.actualExit").value(1430.00))
                .andExpect(jsonPath("$.exitDeviation").value(-20.00))
                .andExpect(jsonPath("$.plannedRisk").value(20.00))
                .andExpect(jsonPath("$.actualProfitLoss").value(1250.00))
                .andExpect(jsonPath("$.targetAchieved").value(false))
                .andExpect(jsonPath("$.stopLossHit").value(false));
    }

    @Test
    void shouldReturnErrorWhenTradeDoesNotExist() throws Exception {

        mockMvc.perform(
                        get("/api/trades/999999/decision-analysis")
                                .with(csrf())
                                .with(user("testuser"))
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Trade not found"));
    }

}