package com.example.tradelens.service;

import com.example.tradelens.entity.Decision;
import com.example.tradelens.entity.Trade;
import com.example.tradelens.repository.TradeRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;




@ExtendWith(MockitoExtension.class)
class DecisionVsActualServiceTest {

    @Mock
    private TradeRepository tradeRepository;

    @InjectMocks
    private DecisionVsActualService decisionVsActualService;


    @Test
    void shouldAnalyzeDecisionVsActual() {

        Trade trade = new Trade();
        trade.setId(1L);
        trade.setEntryPrice(new BigDecimal("1405.00"));
        trade.setExitPrice(new BigDecimal("1430.00"));
        trade.setProfitLoss(new BigDecimal("1250.00"));

        Decision decision = new Decision();
        decision.setPlannedEntry(new BigDecimal("1400.00"));
        decision.setTarget(new BigDecimal("1450.00"));
        decision.setStopLoss(new BigDecimal("1380.00"));

        trade.setDecision(decision);

        when(tradeRepository.findById(1L))
                .thenReturn(Optional.of(trade));

        var response = decisionVsActualService.analyze(1L);

        assertEquals(new BigDecimal("1400.00"), response.plannedEntry());
        assertEquals(new BigDecimal("1405.00"), response.actualEntry());
        assertEquals(new BigDecimal("5.00"), response.entryDeviation());

        assertEquals(new BigDecimal("1450.00"), response.target());
        assertEquals(new BigDecimal("1430.00"), response.actualExit());
        assertEquals(new BigDecimal("-20.00"), response.exitDeviation());

        assertEquals(new BigDecimal("20.00"), response.plannedRisk());
        assertEquals(new BigDecimal("1250.00"), response.actualProfitLoss());

        assertFalse(response.targetAchieved());
        assertFalse(response.stopLossHit());
    }

    @Test
    void shouldDetectTargetAchieved() {

        Trade trade = new Trade();
        trade.setId(1L);
        trade.setEntryPrice(new BigDecimal("1400.00"));
        trade.setExitPrice(new BigDecimal("1450.00"));
        trade.setProfitLoss(new BigDecimal("5000.00"));

        Decision decision = new Decision();
        decision.setPlannedEntry(new BigDecimal("1400.00"));
        decision.setTarget(new BigDecimal("1450.00"));
        decision.setStopLoss(new BigDecimal("1380.00"));

        trade.setDecision(decision);

        when(tradeRepository.findById(1L))
                .thenReturn(Optional.of(trade));

        var response = decisionVsActualService.analyze(1L);

        assertTrue(response.targetAchieved());
        assertFalse(response.stopLossHit());
    }

    @Test
    void shouldDetectStopLossHit() {

        Trade trade = new Trade();
        trade.setId(1L);
        trade.setEntryPrice(new BigDecimal("1400.00"));
        trade.setExitPrice(new BigDecimal("1380.00"));
        trade.setProfitLoss(new BigDecimal("-2000.00"));

        Decision decision = new Decision();
        decision.setPlannedEntry(new BigDecimal("1400.00"));
        decision.setTarget(new BigDecimal("1450.00"));
        decision.setStopLoss(new BigDecimal("1380.00"));

        trade.setDecision(decision);

        when(tradeRepository.findById(1L))
                .thenReturn(Optional.of(trade));

        var response = decisionVsActualService.analyze(1L);

        assertFalse(response.targetAchieved());
        assertTrue(response.stopLossHit());
    }

}