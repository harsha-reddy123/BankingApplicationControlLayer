package com.bacl.domain;

import java.time.Instant;
import java.util.Objects;
import java.util.Optional;

/**
 * DESIGN.md §4.1. {@code verified = false} escalates (R-PAY-01); a recent
 * {@code detailsChangedAt} escalates (R-PAY-03). Empty means the details never changed.
 */
public record Payee(String id,
                    String customerId,
                    String name,
                    String accountNumber,
                    boolean verified,
                    Instant addedAt,
                    Optional<Instant> detailsChangedAt) {

    public Payee {
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(customerId, "customerId");
        Objects.requireNonNull(name, "name");
        Objects.requireNonNull(accountNumber, "accountNumber");
        Objects.requireNonNull(addedAt, "addedAt");
        Objects.requireNonNull(detailsChangedAt, "detailsChangedAt");
    }
}
