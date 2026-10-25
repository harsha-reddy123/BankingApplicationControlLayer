package com.bacl.domain;

import java.math.BigDecimal;
import java.util.Objects;

/** DESIGN.md §4.1. Each account is governed by exactly one {@link Limits}. */
public record Account(String id,
                      String customerId,
                      BigDecimal balance,
                      AccountStatus status,
                      boolean hasLien,
                      Limits limits) {

    public Account {
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(customerId, "customerId");
        Money.requireNonNegative(balance, "balance");
        Objects.requireNonNull(status, "status");
        Objects.requireNonNull(limits, "limits");
    }

    /** Records are immutable, so a balance change produces a new Account. */
    public Account withBalance(BigDecimal newBalance) {
        return new Account(id, customerId, newBalance, status, hasLien, limits);
    }
}
