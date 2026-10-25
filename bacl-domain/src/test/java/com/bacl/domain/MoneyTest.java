package com.bacl.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;
import org.junit.jupiter.api.Test;

class MoneyTest {

    @Test
    void bigDecimalEqualsIsScaleSensitive_soMoneyIsComparedWithCompareTo() {
        BigDecimal two = new BigDecimal("2.0");
        BigDecimal twoCents = new BigDecimal("2.00");
        assertThat(two.equals(twoCents)).isFalse();
        assertThat(two.compareTo(twoCents)).isZero();
    }

    @Test
    void negativeBalanceIsRejected() {
        Limits limits = new Limits(new BigDecimal("5000"), new BigDecimal("5000"), new BigDecimal("500"));
        assertThatThrownBy(() -> new Account("A1", "C1", new BigDecimal("-0.01"), AccountStatus.ACTIVE, false, limits))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("balance");
    }
}
