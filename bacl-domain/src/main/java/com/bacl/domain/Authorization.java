package com.bacl.domain;

import java.time.Instant;
import java.util.Objects;

/**
 * DESIGN.md §4.2. Issued by the control layer on ALLOW and presented to the bank with the action
 * it authorizes. INV-6: only ALLOW decisions carry one.
 */
public record Authorization(String authorizationId, String actionId, Instant issuedAt) {

    public Authorization {
        Objects.requireNonNull(authorizationId, "authorizationId");
        Objects.requireNonNull(actionId, "actionId");
        Objects.requireNonNull(issuedAt, "issuedAt");
    }

    public boolean covers(RequestedAction action) {
        return actionId.equals(action.actionId());
    }
}
