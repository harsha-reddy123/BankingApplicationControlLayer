package com.bacl.domain;

import java.math.BigDecimal;

/** DESIGN.md §4.1. Hard limits checked by R-AMT-01/02 and R-DRF-02, and re-checked by the bank (ADR-004). */
public record Limits(BigDecimal perTransactionLimit,
                     BigDecimal dailyTransferLimit,
                     BigDecimal minBalanceBuffer) {

    public Limits {
        Money.requirePositive(perTransactionLimit, "perTransactionLimit");
        Money.requirePositive(dailyTransferLimit, "dailyTransferLimit");
        Money.requireNonNegative(minBalanceBuffer, "minBalanceBuffer");
    }
}
