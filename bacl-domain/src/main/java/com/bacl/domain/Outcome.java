package com.bacl.domain;

/**
 * DESIGN.md §4.1, §4.3. Declared in ascending severity: {@code ALLOW < ESCALATE < BLOCK}.
 * The pipeline merges outcomes by taking the strictest one, so declaration order matters.
 */
public enum Outcome {
    ALLOW,
    ESCALATE,
    BLOCK;

    public boolean isStricterThan(Outcome other) {
        return compareTo(other) > 0;
    }

    /** The stricter of two outcomes. Used for "final = max(rules, LLM)" (INV-1). */
    public static Outcome strictest(Outcome a, Outcome b) {
        return a.compareTo(b) >= 0 ? a : b;
    }
}
