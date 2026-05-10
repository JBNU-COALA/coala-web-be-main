package com.example.coalawebbackend.domain.user.entity;

public enum UserRole {
    USER,
    STAFF,
    SUPER_ADMIN;

    public String authority() {
        return "ROLE_" + name();
    }

    public boolean atLeast(UserRole role) {
        return ordinal() >= role.ordinal();
    }

    public boolean canModerate() {
        return atLeast(STAFF);
    }
}
