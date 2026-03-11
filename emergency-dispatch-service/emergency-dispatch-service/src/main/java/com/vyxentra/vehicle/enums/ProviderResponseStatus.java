package com.vyxentra.vehicle.enums;


public enum ProviderResponseStatus {
    PENDING("Waiting for response"),
    ACCEPTED("Provider accepted"),
    REJECTED("Provider rejected"),
    TIMEOUT("Provider did not respond"),
    NOTIFIED("Provider notified");

    private final String description;

    ProviderResponseStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
