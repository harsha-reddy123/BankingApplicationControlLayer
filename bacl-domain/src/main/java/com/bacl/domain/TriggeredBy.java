package com.bacl.domain;

/** DESIGN.md §4.1, §4.3. Which stage of the pipeline produced the final decision. */
public enum TriggeredBy {
    RULES,
    LLM,
    /** The judge failed, timed out or returned invalid output (INV-3). */
    FAILSAFE
}
