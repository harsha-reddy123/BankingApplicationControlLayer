package com.bacl.domain;

import java.time.Instant;
import java.util.List;
import java.util.Objects;

/**
 * DESIGN.md §4.1, §4.2. The audit entry written for every {@code evaluate} call (INV-5).
 * Carries the run, turn, action and autonomy level so the evaluation harness can score it
 * per category and per autonomy level without joining against anything else.
 */
public record DecisionRecord(String recordId,
                             String runId,
                             int turn,
                             String actionId,
                             AutonomyLevel autonomy,
                             Outcome outcome,
                             String reason,
                             double confidence,
                             TriggeredBy triggeredBy,
                             List<String> ruleIds,
                             Instant recordedAt) {

    public DecisionRecord {
        Objects.requireNonNull(recordId, "recordId");
        Objects.requireNonNull(runId, "runId");
        Objects.requireNonNull(actionId, "actionId");
        Objects.requireNonNull(autonomy, "autonomy");
        Objects.requireNonNull(outcome, "outcome");
        Objects.requireNonNull(reason, "reason");
        Objects.requireNonNull(triggeredBy, "triggeredBy");
        ruleIds = List.copyOf(Objects.requireNonNull(ruleIds, "ruleIds"));
        Objects.requireNonNull(recordedAt, "recordedAt");
    }

    public static DecisionRecord of(String recordId, String runId, RequestedAction action, AutonomyLevel autonomy,
                                    Decision decision, Instant recordedAt) {
        return new DecisionRecord(recordId, runId, action.turn(), action.actionId(), autonomy,
                decision.outcome(), decision.reason(), decision.confidence(), decision.triggeredBy(),
                decision.ruleIds(), recordedAt);
    }
}
