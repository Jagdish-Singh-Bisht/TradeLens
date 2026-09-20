package com.example.tradelens.service;


import com.example.tradelens.entity.Execution;
import lombok.Getter;

import java.math.BigDecimal;


@Getter
public class OpenPosition {

    private final Execution execution;

    private BigDecimal remainingQuantity;

    public OpenPosition(Execution execution) {
        this.execution = execution;
        this.remainingQuantity = execution.getQuantity();
    }

    public void reduceQuantity(BigDecimal quantity) {
        this.remainingQuantity = this.remainingQuantity
                .subtract(quantity);
    }

}
