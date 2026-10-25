package com.bacl.domain;

import java.util.Objects;

/** DESIGN.md §4.1. */
public record Customer(String id, String name) {

    public Customer {
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(name, "name");
    }
}
