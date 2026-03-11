package com.vyxentra.vehicle.enums;


public enum UserStatus {
    ACTIVE("Active"),
    INACTIVE("Inactive"),
    SUSPENDED("Suspended"),
    BLOCKED("Blocked"),
    DELETED("Deleted"),
    PENDING_VERIFICATION("Pending Verification");

    private final String displayName;

    UserStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public boolean canPerformActions() {
        return this == ACTIVE;
    }

    public boolean isPending() {
        return this == PENDING_VERIFICATION;
    }

    public boolean isSuspendedOrBlocked() {
        return this == SUSPENDED || this == BLOCKED;
    }
}