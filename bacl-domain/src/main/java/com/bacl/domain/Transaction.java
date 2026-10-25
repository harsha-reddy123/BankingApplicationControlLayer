package com.bacl.domain;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Objects;

/** DESIGN.md §4.1. One entry in the bank's transaction log; the idempotency key prevents double execution. */
public record Transaction(String id,
                          String fromAccount,
                          String toPayee,
                          BigDecimal amount,
                          String idempotencyKey,
                          TransactionStatus status,
                          Instant at) {

    public Transaction {
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(fromAccount, "fromAccount");
        Objects.requireNonNull(toPayee, "toPayee");
        Money.requirePositive(amount, "amount");
        Objects.requireNonNull(idempotencyKey, "idempotencyKey");
        Objects.requireNonNull(status, "status");
        Objects.requireNonNull(at, "at");
    }
}
