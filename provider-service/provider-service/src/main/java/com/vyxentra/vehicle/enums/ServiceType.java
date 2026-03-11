package com.vyxentra.vehicle.enums;


public enum ServiceType {
    GENERAL_SERVICE("General Service", ProviderType.SERVICE_CENTER),
    ENGINE_REPAIR("Engine Repair", ProviderType.SERVICE_CENTER),
    BRAKE_REPAIR("Brake Repair", ProviderType.SERVICE_CENTER),
    BATTERY_REPLACEMENT("Battery Replacement", ProviderType.SERVICE_CENTER),
    TIRE_REPLACEMENT("Tire Replacement", ProviderType.SERVICE_CENTER),
    AC_SERVICE("AC Service", ProviderType.SERVICE_CENTER),
    TRANSMISSION_REPAIR("Transmission Repair", ProviderType.SERVICE_CENTER),
    ELECTRICAL_REPAIR("Electrical Repair", ProviderType.SERVICE_CENTER),
    BASIC_WASH("Basic Wash", ProviderType.WASHING_CENTER),
    PREMIUM_WASH("Premium Wash", ProviderType.WASHING_CENTER),
    DETAILING("Detailing", ProviderType.WASHING_CENTER),
    INTERIOR_CLEANING("Interior Cleaning", ProviderType.WASHING_CENTER),
    EXTERIOR_POLISHING("Exterior Polishing", ProviderType.WASHING_CENTER),
    CERAMIC_COATING("Ceramic Coating", ProviderType.WASHING_CENTER),
    ROADSIDE_ASSISTANCE("Roadside Assistance", ProviderType.EMERGENCY_SERVICE),
    TOWING("Towing", ProviderType.TOWING_SERVICE),
    FUEL_DELIVERY("Fuel Delivery", ProviderType.FUEL_DELIVERY),
    TIRE_PUNCTURE("Tire Puncture Repair", ProviderType.EMERGENCY_SERVICE),
    COMPLETE_SERVICE("Complete Service", ProviderType.BOTH);

    private final String displayName;
    private final ProviderType defaultProviderType;

    ServiceType(String displayName, ProviderType defaultProviderType) {
        this.displayName = displayName;
        this.defaultProviderType = defaultProviderType;
    }

    public String getDisplayName() {
        return displayName;
    }

    public ProviderType getDefaultProviderType() {
        return defaultProviderType;
    }

    public boolean isEmergency() {
        return this == ROADSIDE_ASSISTANCE || this == TOWING ||
                this == FUEL_DELIVERY || this == TIRE_PUNCTURE;
    }

    public boolean isWashing() {
        return this == BASIC_WASH || this == PREMIUM_WASH || this == DETAILING ||
                this == INTERIOR_CLEANING || this == EXTERIOR_POLISHING || this == CERAMIC_COATING;
    }

    public boolean isRepair() {
        return !isEmergency() && !isWashing();
    }
}
