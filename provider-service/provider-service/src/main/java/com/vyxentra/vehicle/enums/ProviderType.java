package com.vyxentra.vehicle.enums;


public enum ProviderType {
    SERVICE_CENTER("Service Center", true, true),
    WASHING_CENTER("Washing Center", true, false),
    GARAGE("Garage", true, false),
    TOWING_SERVICE("Towing Service", false, true),
    FUEL_DELIVERY("Fuel Delivery", false, true),
    EMERGENCY_SERVICE("Emergency Service", true, true),
    BOTH("Both Service and Washing", true, true);

    private final String displayName;
    private final boolean canProvideService;
    private final boolean canProvideEmergency;

    ProviderType(String displayName, boolean canProvideService, boolean canProvideEmergency) {
        this.displayName = displayName;
        this.canProvideService = canProvideService;
        this.canProvideEmergency = canProvideEmergency;
    }

    public String getDisplayName() {
        return displayName;
    }

    public boolean canProvideService() {
        return canProvideService;
    }

    public boolean canProvideEmergency() {
        return canProvideEmergency;
    }
}