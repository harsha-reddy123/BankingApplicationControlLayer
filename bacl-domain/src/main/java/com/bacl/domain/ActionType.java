package com.bacl.domain;

/** DESIGN.md §4.1. The kinds of write action an agent can request. */
public enum ActionType {
    PAYMENT,
    TRANSFER,
    LOAN_MODIFICATION,
    ACCOUNT_CHANGE;

    /** Payments and transfers move money, so they must carry a payee and an amount. */
    public boolean movesMoney() {
        return this == PAYMENT || this == TRANSFER;
    }
}
