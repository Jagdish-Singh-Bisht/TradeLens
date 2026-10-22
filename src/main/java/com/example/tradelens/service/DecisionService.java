package com.example.tradelens.service;


import com.example.tradelens.entity.Trade;
import com.example.tradelens.entity.Decision;
import com.example.tradelens.repository.DecisionRepository;
import com.example.tradelens.repository.TradeRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;


@Service
public class DecisionService {

    private final DecisionRepository decisionRepository;
    private final TradeRepository tradeRepository;

    public DecisionService(DecisionRepository decisionRepository,
                           TradeRepository tradeRepository) {

        this.decisionRepository = decisionRepository;
        this.tradeRepository = tradeRepository;
    }

    public void createDecision(Long tradeId,
                               String username,
                               Decision decision) {

        Trade trade = tradeRepository.
                findByIdAndBrokerAccount_User_Username(
                        tradeId,
                        username
                )
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Trade not found"
                ));

        Decision existingDecision = trade.getDecision();

        if(existingDecision != null) {
            decision.setId(existingDecision.getId());
        }

        Decision savedDecision = decisionRepository.save(decision);

        trade.setDecision(savedDecision);

        tradeRepository.save(trade);

    }


}
