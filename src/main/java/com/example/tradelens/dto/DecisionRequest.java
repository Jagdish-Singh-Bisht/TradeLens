package com.example.tradelens.dto;


import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Max;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;



@Getter
@Setter
public class DecisionRequest {

    @NotBlank
    private String strategy;

    @NotNull
    @DecimalMin(value = "0.0", inclusive = false)
    private BigDecimal plannedEntry;

    @NotNull
    @DecimalMin(value = "0.0", inclusive = false)
    private BigDecimal target;

    @NotNull
    @DecimalMin(value = "0.0", inclusive = false)
    private BigDecimal stopLoss;

    @NotNull
    @Min(1)
    @Max(10)
    private Integer confidence;

    @NotBlank
    private String entryReason;

    private String exitReason;

}
