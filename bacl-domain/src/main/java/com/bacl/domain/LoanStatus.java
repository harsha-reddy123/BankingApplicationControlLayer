package com.bacl.domain;

/** DESIGN.md §4.1. {@code DELINQUENT} escalates (R-POL-04); {@code DEFAULTED} blocks (R-POL-03). */
public enum LoanStatus {
    CURRENT,
    DELINQUENT,
    DEFAULTED
}
