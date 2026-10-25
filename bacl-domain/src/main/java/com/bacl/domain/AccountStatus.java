package com.bacl.domain;

/** DESIGN.md §4.1. Anything other than {@code ACTIVE} blocks outgoing actions (R-POL-01). */
public enum AccountStatus {
    ACTIVE,
    FROZEN,
    CLOSED
}
