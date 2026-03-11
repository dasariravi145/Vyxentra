package com.vyxentra.vehicle.enums;


public enum PricingAlgorithm {
    FIXED("Fixed Pricing", "Price remains constant"),
    DYNAMIC("Dynamic Pricing", "Price varies based on demand"),
    SURGE("Surge Pricing", "Higher prices during peak hours"),
    TIME_BASED("Time Based", "Different prices for different time slots"),
    DISTANCE_BASED("Distance Based", "Price based on distance"),
    VEHICLE_TYPE_BASED("Vehicle Type Based", "Different prices for different vehicle types"),
    PROMOTIONAL("Promotional", "Discounted prices for promotions");

    private final String displayName;
    private final String description;

    PricingAlgorithm(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getDescription() {
        return description;
    }

    public boolean isDynamic() {
        return this == DYNAMIC || this == SURGE || this == TIME_BASED;
    }
}
