package com.bacl.domain;

/** DESIGN.md §4.1. Result of the bank executing an authorized action. */
public enum TransactionStatus {
    EXECUTED,
    /** The bank's own re-validation refused it, even with an Authorization (ADR-004). */
    REJECTED
}
