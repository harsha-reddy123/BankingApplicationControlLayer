package com.bacl.domain;

import java.math.BigDecimal;
import java.util.Objects;

/** DESIGN.md §4.1. Status drives R-POL-02/03/04. */
public record Loan(String id, String accountId, BigDecimal principal, LoanStatus status) {

    public Loan {
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(accountId, "accountId");
        Money.requireNonNegative(principal, "principal");
        Objects.requireNonNull(status, "status");
    }
}
