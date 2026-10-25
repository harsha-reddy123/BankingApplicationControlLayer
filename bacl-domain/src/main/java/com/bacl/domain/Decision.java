package com.bacl.domain;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * DESIGN.md §4.2. The output of {@code ControlLayer.evaluate}. The constructor enforces the
 * invariants that can be checked on a single decision, so an invalid Decision cannot exist:
 * <ul>
 *   <li>INV-6: ALLOW always carries an Authorization; ESCALATE and BLOCK never do.</li>
 *   <li>INV-3: a FAILSAFE decision is always ESCALATE.</li>
 * </ul>
 */
public record Decision(Outcome outcome,
                       String reason,
                       double confidence,
                       TriggeredBy triggeredBy,
                       List<String> ruleIds,
                       Optional<Authorization> authorization) {

    public Decision {
        Objects.requireNonNull(outcome, "outcome");
        Objects.requireNonNull(reason, "reason");
        Objects.requireNonNull(triggeredBy, "triggeredBy");
        ruleIds = List.copyOf(Objects.requireNonNull(ruleIds, "ruleIds"));
        Objects.requireNonNull(authorization, "authorization");
        if (confidence < 0.0 || confidence > 1.0) {
            throw new IllegalArgumentException("confidence must be in [0, 1], was " + confidence);
        }
        if ((outcome == Outcome.ALLOW) != authorization.isPresent()) {
            throw new IllegalArgumentException("INV-6: ALLOW must carry an Authorization and "
                    + "ESCALATE/BLOCK must not (outcome=" + outcome + ")");
        }
        if (triggeredBy == TriggeredBy.FAILSAFE && outcome != Outcome.ESCALATE) {
            throw new IllegalArgumentException("INV-3: a FAILSAFE decision must be ESCALATE, was " + outcome);
        }
    }

    public static Decision allow(String reason, double confidence, TriggeredBy triggeredBy,
                                 List<String> ruleIds, Authorization authorization) {
        return new Decision(Outcome.ALLOW, reason, confidence, triggeredBy, ruleIds, Optional.of(authorization));
    }

    public static Decision escalate(String reason, double confidence, TriggeredBy triggeredBy, List<String> ruleIds) {
        return new Decision(Outcome.ESCALATE, reason, confidence, triggeredBy, ruleIds, Optional.empty());
    }

    public static Decision block(String reason, double confidence, TriggeredBy triggeredBy, List<String> ruleIds) {
        return new Decision(Outcome.BLOCK, reason, confidence, triggeredBy, ruleIds, Optional.empty());
    }

    /** INV-3: judge error, timeout or invalid output. */
    public static Decision failsafe(String reason, List<String> ruleIds) {
        return escalate(reason, 0.0, TriggeredBy.FAILSAFE, ruleIds);
    }
}
