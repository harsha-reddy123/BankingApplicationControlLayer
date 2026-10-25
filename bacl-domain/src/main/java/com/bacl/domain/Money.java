package com.bacl.domain;

import java.math.BigDecimal;
import java.util.Objects;

/**
 * Validation helpers for money values. Money is always {@link BigDecimal}, never double, and is
 * compared with {@code compareTo}, never {@code equals} (2.0 and 2.00 are not equal()).
 */
public final class Money {

    private Money() {
    }

    public static BigDecimal requireNonNegative(BigDecimal value, String name) {
        Objects.requireNonNull(value, name);
        if (value.signum() < 0) {
            throw new IllegalArgumentException(name + " must be >= 0, was " + value);
        }
        return value;
    }

    public static BigDecimal requirePositive(BigDecimal value, String name) {
        Objects.requireNonNull(value, name);
        if (value.signum() <= 0) {
            throw new IllegalArgumentException(name + " must be > 0, was " + value);
        }
        return value;
    }
}
