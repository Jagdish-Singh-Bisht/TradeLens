package com.example.tradelens.service;



import com.example.tradelens.entity.Trade;
import com.example.tradelens.entity.Execution;
import com.example.tradelens.entity.enums.ExecutionSide;
import com.example.tradelens.repository.TradeRepository;

import org.springframework.stereotype.Service;

import java.util.LinkedList;
import java.util.Queue;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;





@Service
public class TradeReconstructionService  {

    private final Queue<OpenPosition> openPositions = new LinkedList<>();

    private final TradeRepository tradeRepository;


    public TradeReconstructionService(TradeRepository tradeRepository) {
        this.tradeRepository = tradeRepository;
    }

    public void processExecution(Execution execution) {

        if(execution.getSide() == ExecutionSide.BUY) {
            openPositions.add(new OpenPosition(execution));
        }

        else if(execution.getSide() == ExecutionSide.SELL) {

            if(openPositions.isEmpty()) {
                throw new IllegalStateException(
                        "Cannot process SELL without an open position"
                );
            }

            BigDecimal remainingSellQuantity = execution.getQuantity();

            BigDecimal totalMatchedQuantity = BigDecimal.ZERO;
            BigDecimal totalEntryValue = BigDecimal.ZERO;
            BigDecimal totalProfitLoss = BigDecimal.ZERO;

            LocalDateTime entryTime = null;

            while (remainingSellQuantity.compareTo(BigDecimal.ZERO) > 0
                    && !openPositions.isEmpty()) {

                OpenPosition position = openPositions.peek();

                if(entryTime == null) {
                    entryTime = position.getExecution().getExecutedAt();
                }

                BigDecimal matchedQuantity = position.getRemainingQuantity()
                        .min(remainingSellQuantity);

                totalMatchedQuantity = totalMatchedQuantity.add(matchedQuantity);

                BigDecimal matchedEntryValue = position.getExecution().getPrice()
                        .multiply(matchedQuantity);

                totalEntryValue = totalEntryValue.add(matchedEntryValue);

                BigDecimal matchedProfitLoss = execution.getPrice() // SELL/exit price
                        .subtract(position.getExecution().getPrice()) // BUY/entry price
                        .multiply(matchedQuantity);

                totalProfitLoss = totalProfitLoss.add(matchedProfitLoss);

                BigDecimal remainingBuyQuantity = position.getRemainingQuantity()
                        .subtract(matchedQuantity);

                position.reduceQuantity(matchedQuantity);

                remainingSellQuantity = remainingSellQuantity
                        .subtract(matchedQuantity);

                if(remainingBuyQuantity.compareTo(BigDecimal.ZERO) == 0) {
                    openPositions.poll();
                }
            }

            if(remainingSellQuantity.compareTo(BigDecimal.ZERO) > 0) {
                throw new IllegalStateException(
                        "SELL quantity exceeds available open position"
                );
            }

            BigDecimal averageEntryPrice = totalEntryValue.divide(
                    totalMatchedQuantity,
                    2,
                    RoundingMode.HALF_UP
            );

            Trade trade = new Trade();

            trade.setInstrument(execution.getInstrument());
            trade.setBrokerAccount(execution.getBrokerAccount());
            trade.setQuantity(totalMatchedQuantity);
            trade.setEntryPrice(averageEntryPrice);
            trade.setExitPrice(execution.getPrice());
            trade.setEntryTime(entryTime);
            trade.setExitTime(execution.getExecutedAt());
            trade.setProfitLoss(totalProfitLoss);

            tradeRepository.save(trade);

        }
    }


}
