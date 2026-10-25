package com.bacl.domain;

import java.math.BigDecimal;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

/**
 * DESIGN.md §4.2. A request packaged as an object (Command): the control layer can evaluate it,
 * the bank can execute it and the audit log can replay it.
 *
 * <p>{@code payee} and {@code amount} are optional because loan modifications and account
 * changes have neither (ADR-010). Money-moving actions must have both. Type-specific inputs such
 * as {@code loanId} go in {@code params}.
 */
public record RequestedAction(String actionId,
                              ActionType type,
                              Field<String> fromAccount,
                              Optional<Field<String>> payee,
                              Optional<Field<BigDecimal>> amount,
                              Map<String, Field<String>> params,
                              int turn) {

    public RequestedAction {
        Objects.requireNonNull(actionId, "actionId");
        Objects.requireNonNull(type, "type");
        Objects.requireNonNull(fromAccount, "fromAccount");
        Objects.requireNonNull(payee, "payee");
        Objects.requireNonNull(amount, "amount");
        params = Map.copyOf(Objects.requireNonNull(params, "params"));
        if (turn < 1) {
            throw new IllegalArgumentException("turn must be >= 1, was " + turn);
        }
        if (type.movesMoney() && (payee.isEmpty() || amount.isEmpty())) {
            throw new IllegalArgumentException(type + " requires a payee and an amount");
        }
        amount.ifPresent(a -> Money.requirePositive(a.value(), "amount"));
    }

    /** A PAYMENT or TRANSFER. */
    public static RequestedAction moneyMovement(String actionId, ActionType type, Field<String> fromAccount,
                                                Field<String> payee, Field<BigDecimal> amount, int turn) {
        return new RequestedAction(actionId, type, fromAccount, Optional.of(payee), Optional.of(amount),
                Map.of(), turn);
    }

    /** A LOAN_MODIFICATION or ACCOUNT_CHANGE, described entirely by its params. */
    public static RequestedAction withParams(String actionId, ActionType type, Field<String> fromAccount,
                                             Map<String, Field<String>> params, int turn) {
        return new RequestedAction(actionId, type, fromAccount, Optional.empty(), Optional.empty(), params, turn);
    }

    /** Every field on this action, for provenance checks such as judge trigger T-1. */
    public Stream<Field<?>> allFields() {
        return Stream.of(Stream.<Field<?>>of(fromAccount), payee.stream(), amount.stream(), params.values().stream())
                .flatMap(s -> s);
    }
}
