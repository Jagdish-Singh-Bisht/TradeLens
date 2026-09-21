package com.example.tradelens.service;


import com.example.tradelens.entity.Trade;
import com.example.tradelens.entity.Decision;
import com.example.tradelens.repository.DecisionRepository;
import com.example.tradelens.repository.TradeRepository;
import org.springframework.stereotype.Service;



@Service
public class DecisionService {

    private final DecisionRepository decisionRepository;
    private final TradeRepository tradeRepository;

    public DecisionService(DecisionRepository decisionRepository,
                           TradeRepository tradeRepository) {

        this.decisionRepository = decisionRepository;
        this.tradeRepository = tradeRepository;
    }

    public void createDecision(Long tradeId, Decision decision) {

        Trade trade = tradeRepository.findById(tradeId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Trade not found"
                ));

        Decision savedDecision = decisionRepository.save(decision);

        trade.setDecision(savedDecision);

        tradeRepository.save(trade);

    }


}
