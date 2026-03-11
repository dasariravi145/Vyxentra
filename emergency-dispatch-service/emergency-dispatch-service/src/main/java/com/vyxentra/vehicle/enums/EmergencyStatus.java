package com.vyxentra.vehicle.enums;


public enum EmergencyStatus {
    SEARCHING("Searching for providers"),
    ASSIGNED("Provider assigned"),
    PROVIDER_ARRIVED("Provider arrived"),
    IN_PROGRESS("Service in progress"),
    COMPLETED("Completed"),
    CANCELLED("Cancelled"),
    EXPIRED("Expired"),
    FAILED("Failed");

    private final String description;

    EmergencyStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    public boolean isActive() {
        return this == SEARCHING || this == ASSIGNED ||
                this == PROVIDER_ARRIVED || this == IN_PROGRESS;
    }

    public boolean isTerminal() {
        return this == COMPLETED || this == CANCELLED ||
                this == EXPIRED || this == FAILED;
    }
}
