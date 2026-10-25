package com.bacl.domain;

import java.util.Objects;

/** DESIGN.md §4.1 "Field provenance", ADR-005. A value tagged with where it came from. */
public record Field<T>(T value, Provenance provenance) {

    public Field {
        Objects.requireNonNull(value, "value");
        Objects.requireNonNull(provenance, "provenance");
    }

    public static <T> Field<T> customerStated(T value) {
        return new Field<>(value, Provenance.CUSTOMER_STATED);
    }

    public static <T> Field<T> agentInferred(T value) {
        return new Field<>(value, Provenance.AGENT_INFERRED);
    }

    public static <T> Field<T> externalContent(T value) {
        return new Field<>(value, Provenance.EXTERNAL_CONTENT);
    }

    public boolean is(Provenance p) {
        return provenance == p;
    }
}
