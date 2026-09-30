package com.example.tradelens.dto;

import java.math.BigDecimal;

public record PostExitMovementResponse(
        BigDecimal exitPrice,
        BigDecimal highestPriceAfterExit,
        BigDecimal postExitMovement
) {
}
