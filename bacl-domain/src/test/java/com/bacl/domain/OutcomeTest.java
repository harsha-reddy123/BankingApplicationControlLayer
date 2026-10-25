package com.bacl.domain;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class OutcomeTest {

    @Test
    void severityOrder_isAllowThenEscalateThenBlock() {
        assertThat(Outcome.BLOCK.isStricterThan(Outcome.ESCALATE)).isTrue();
        assertThat(Outcome.ESCALATE.isStricterThan(Outcome.ALLOW)).isTrue();
        assertThat(Outcome.ALLOW.isStricterThan(Outcome.ALLOW)).isFalse();
    }

    @Test
    void inv1_strictestNeverRelaxesEitherInput() {
        for (Outcome a : Outcome.values()) {
            for (Outcome b : Outcome.values()) {
                Outcome merged = Outcome.strictest(a, b);
                assertThat(merged).isGreaterThanOrEqualTo(a).isGreaterThanOrEqualTo(b);
            }
        }
    }
}
