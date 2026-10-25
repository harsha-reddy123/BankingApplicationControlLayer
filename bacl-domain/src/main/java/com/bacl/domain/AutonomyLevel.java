package com.bacl.domain;

/**
 * DESIGN.md §4.6. How much the agent may do without asking first. Constant names match the
 * scenario files ({@code L0}..{@code L3}). Dollar thresholds per level are policy, so they live
 * in {@code AutonomyPolicy} (M2), not here.
 */
public enum AutonomyLevel {
    L0("Suggest-only"),
    L1("Supervised"),
    L2("Delegated"),
    L3("Autonomous");

    private final String displayName;

    AutonomyLevel(String displayName) {
        this.displayName = displayName;
    }

    public String displayName() {
        return displayName;
    }
}
