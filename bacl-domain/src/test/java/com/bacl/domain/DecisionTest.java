package com.bacl.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class DecisionTest {

    private static final Authorization AUTH = new Authorization("AUTH-1", "ACT-1", Instant.EPOCH);

    @Test
    void inv6_allowWithoutAuthorizationIsRejected() {
        assertThatThrownBy(() -> new Decision(Outcome.ALLOW, "ok", 1.0, TriggeredBy.RULES, List.of(), Optional.empty()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("INV-6");
    }

    @Test
    void inv6_escalateAndBlockWithAuthorizationAreRejected() {
        for (Outcome outcome : List.of(Outcome.ESCALATE, Outcome.BLOCK)) {
            assertThatThrownBy(() -> new Decision(outcome, "no", 1.0, TriggeredBy.RULES, List.of(), Optional.of(AUTH)))
                    .hasMessageContaining("INV-6");
        }
    }

    @Test
    void inv6_factoriesProduceValidDecisions() {
        assertThat(Decision.allow("ok", 1.0, TriggeredBy.RULES, List.of(), AUTH).authorization()).contains(AUTH);
        assertThat(Decision.escalate("ask", 1.0, TriggeredBy.RULES, List.of("R-PAY-01")).authorization()).isEmpty();
        assertThat(Decision.block("no", 1.0, TriggeredBy.RULES, List.of("R-AMT-01")).authorization()).isEmpty();
    }

    @Test
    void inv3_failsafeIsAlwaysEscalate() {
        assertThat(Decision.failsafe("judge timed out", List.of()).outcome()).isEqualTo(Outcome.ESCALATE);
        assertThatThrownBy(() -> Decision.block("no", 0.0, TriggeredBy.FAILSAFE, List.of()))
                .hasMessageContaining("INV-3");
    }

    @Test
    void confidenceOutsideZeroToOneIsRejected() {
        assertThatThrownBy(() -> Decision.escalate("x", 1.5, TriggeredBy.LLM, List.of()))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void ruleIdsAreDefensivelyCopied() {
        List<String> ids = new ArrayList<>(List.of("R-AMT-01"));
        Decision d = Decision.block("no", 1.0, TriggeredBy.RULES, ids);
        ids.add("R-POL-01");
        assertThat(d.ruleIds()).containsExactly("R-AMT-01");
    }
}
