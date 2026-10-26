package com.example.tradelens.service;



import com.example.tradelens.entity.BrokerAccount;
import com.example.tradelens.entity.Trade;
import com.example.tradelens.entity.Execution;
import com.example.tradelens.entity.enums.ExecutionSide;
import com.example.tradelens.repository.ExecutionRepository;
import com.example.tradelens.repository.TradeRepository;

import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.Queue;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;


@Service
public class TradeReconstructionService  {

    private final Map<PositionKey, Queue<OpenPosition>> openPositions = new HashMap<>();

    private final TradeRepository tradeRepository;

    private final ExecutionRepository executionRepository;

    private final Set<PositionKey> initializedPositions = new HashSet<>();

    public TradeReconstructionService(TradeRepository tradeRepository,
                                      ExecutionRepository executionRepository) {

        this.tradeRepository = tradeRepository;
        this.executionRepository = executionRepository;
    }

    public void processExecution(Execution execution) {

        PositionKey key = new PositionKey(
                execution.getBrokerAccount().getId(),
                execution.getInstrument().getId()
        );

        Queue<OpenPosition> existingQueue = openPositions.get(key);

        Queue<OpenPosition> queue = copyQueue(
                existingQueue != null ? existingQueue : new LinkedList<>()
        );

        if(execution.getSide() == ExecutionSide.BUY) {
            invalidatePositionsOnRollback(key);
            queue.add(new OpenPosition(execution));
            openPositions.put(key, queue);
        }

        else if(execution.getSide() == ExecutionSide.SELL) {

            if(queue.isEmpty()) {
                throw new IllegalStateException(
                        "Cannot process SELL without an open position"
                );
            }

            BigDecimal remainingSellQuantity = execution.getQuantity();

            BigDecimal availableBuyQuantity = queue.stream()
                    .map(OpenPosition::getRemainingQuantity)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            if(remainingSellQuantity.compareTo(availableBuyQuantity) > 0) {
                throw new IllegalStateException(
                        "Sell quantity exceeds available open position"
                );
            }

            BigDecimal totalMatchedQuantity = BigDecimal.ZERO;
            BigDecimal totalEntryValue = BigDecimal.ZERO;
            BigDecimal totalProfitLoss = BigDecimal.ZERO;

            LocalDateTime entryTime = null;

            while (remainingSellQuantity.compareTo(BigDecimal.ZERO) > 0
                    && !queue.isEmpty()) {

                OpenPosition position = queue.peek();

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
                    queue.poll();
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

            if(queue.isEmpty()) {
                openPositions.remove(key);
            } else {
                openPositions.put(key, queue);
            }

        }

    }

    public void rebuildOpenPositions(BrokerAccount brokerAccount, Long instrumentId) {

        PositionKey key = new PositionKey(
                brokerAccount.getId(),
                instrumentId
        );

        Queue<OpenPosition> queue = new LinkedList<>();

        List<Execution> executions = executionRepository
                .findByBrokerAccountAndInstrument_Id(
                        brokerAccount,
                        instrumentId,
                        Sort.by(
                                Sort.Order.asc("executedAt"),
                                Sort.Order.asc("id")
                        )
                );

        for(Execution execution : executions) {

            if (execution.getSide() == ExecutionSide.BUY ) {
                queue.add(new OpenPosition(execution));
            }
            else if (execution.getSide() == ExecutionSide.SELL ) {
                BigDecimal remainingSellQuantity = execution.getQuantity();

                while (remainingSellQuantity.compareTo(BigDecimal.ZERO) > 0
                        && !queue.isEmpty() ) {

                    OpenPosition position = queue.peek();

                    BigDecimal matchedQuantity = position.getRemainingQuantity()
                            .min(remainingSellQuantity);

                    position.reduceQuantity(matchedQuantity);

                    remainingSellQuantity = remainingSellQuantity
                            .subtract(matchedQuantity);

                    if (position.getRemainingQuantity()
                            .compareTo(BigDecimal.ZERO) == 0) {

                        queue.poll();
                    }
                }

                if(remainingSellQuantity.compareTo(BigDecimal.ZERO) > 0) {

                    throw new IllegalStateException(
                            "Cannot rebuild positions: SELL quantity exceeds "
                            + "available BUY quantity for execution"
                            + execution.getId()
                    );
                }

            }
        }

        if(queue.isEmpty()) {
            openPositions.remove(key);
        } else {
            openPositions.put(key, queue);
        }

        initializedPositions.add(key);

        System.out.println(
                "REBUILD: key=" + key
                        + ", remaining open quantity="
                        + queue.stream()
                        .map(OpenPosition::getRemainingQuantity)
                        .reduce(BigDecimal.ZERO, BigDecimal::add)
        );

    }

    public void ensurePositionsInitialized(BrokerAccount brokerAccount, Long instrumentId) {

        PositionKey key = new PositionKey(
                brokerAccount.getId(),
                instrumentId
        );

        if(!initializedPositions.contains(key)) {
            rebuildOpenPositions(brokerAccount, instrumentId);
        }

    }

    public Queue<OpenPosition> copyQueue(Queue<OpenPosition> source) {

        Queue<OpenPosition> copy = new LinkedList<>();

        for(OpenPosition position : source) {
            OpenPosition copiedPosition = new OpenPosition(
                    position.getExecution()
            );

            BigDecimal alreadyClosed = position.getExecution()
                    .getQuantity()
                    .subtract(position.getRemainingQuantity());

            if(alreadyClosed.compareTo(BigDecimal.ZERO) > 0) {
                copiedPosition.reduceQuantity(alreadyClosed);
            }

            copy.add(copiedPosition);
        }

        return copy;
    }

    public void invalidatePositionsOnRollback(PositionKey key) {

        if(!TransactionSynchronizationManager.isSynchronizationActive()) {
            return;
        }

        TransactionSynchronizationManager.registerSynchronization(
                new TransactionSynchronization() {
                    @Override
                    public void afterCompletion(int status) {
                        if(status != STATUS_COMMITTED) {
                            openPositions.remove(key);
                            initializedPositions.remove(key);
                        }
                    }
                }
        );

    }


}
