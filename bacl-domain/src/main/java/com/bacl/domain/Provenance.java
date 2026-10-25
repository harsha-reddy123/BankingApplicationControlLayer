package com.bacl.domain;

/** DESIGN.md §4.1 "Field provenance", ADR-005. Where the value of an action field came from. */
public enum Provenance {
    /** The customer said it explicitly. */
    CUSTOMER_STATED,
    /** The agent filled it in from context; fires judge trigger T-1. */
    AGENT_INFERRED,
    /** Came from a document, email or web page; payee fields with this escalate (R-PAY-02). */
    EXTERNAL_CONTENT
}
