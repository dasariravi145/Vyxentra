package com.vyxentra.vehicle.enums;


public enum ProviderStatus {
    PENDING_APPROVAL("Pending Approval"),
    ACTIVE("Active"),
    SUSPENDED("Suspended"),
    REJECTED("Rejected"),
    INACTIVE("Inactive"),
    DOCUMENTS_PENDING("Documents Pending"),
    VERIFICATION_PENDING("Verification Pending"),
    APPROVED("Approved"),
    BLOCKED("Blocked"),
    DELETED("Deleted");

    private final String displayName;

    ProviderStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public boolean canAcceptBookings() {
        return this == ACTIVE || this == APPROVED;
    }

    public boolean isPending() {
        return this == PENDING_APPROVAL || this == DOCUMENTS_PENDING || this == VERIFICATION_PENDING;
    }

    public boolean isActive() {
        return this == ACTIVE || this == APPROVED;
    }

    public boolean isSuspended() {
        return this == SUSPENDED || this == BLOCKED;
    }
}
