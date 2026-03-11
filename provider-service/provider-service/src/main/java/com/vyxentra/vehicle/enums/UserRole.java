package com.vyxentra.vehicle.enums;


public enum UserRole {
    CUSTOMER("Customer"),
    PROVIDER("Provider"),
    EMPLOYEE("Employee"),
    ADMIN("Administrator"),
    SUPER_ADMIN("Super Administrator");

    private final String displayName;

    UserRole(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public boolean isAdmin() {
        return this == ADMIN || this == SUPER_ADMIN;
    }
}
