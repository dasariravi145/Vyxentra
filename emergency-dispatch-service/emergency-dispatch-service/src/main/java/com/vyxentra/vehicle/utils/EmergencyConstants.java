package com.vyxentra.vehicle.utils;


public final class EmergencyConstants {

    private EmergencyConstants() {}

    // Redis Geo Indices
    public static final String BIKE_REPAIR_INDEX = "bike:repair";
    public static final String CAR_REPAIR_INDEX = "car:repair";
    public static final String BIKE_FUEL_INDEX = "bike:fuel";
    public static final String CAR_FUEL_INDEX = "car:fuel";
    public static final String PROVIDER_GEO_INDEX = "providers:geo";

    // Redis Keys
    public static final String EMERGENCY_REQUEST_PREFIX = "emergency:request:";
    public static final String EMERGENCY_LOCK_PREFIX = "lock:emergency:";
    public static final String PROVIDER_TIMEOUT_PREFIX = "emergency:timeout:";
    public static final String PROVIDER_LOCATION_PREFIX = "provider:location:";
    public static final String PROVIDER_AVAILABILITY_PREFIX = "provider:available:";
    public static final String PROVIDER_DETAILS_PREFIX = "provider:details:";

    // Kafka Topics
    public static final String EMERGENCY_TRIGGERED_TOPIC = "emergency.triggered";
    public static final String EMERGENCY_ASSIGNED_TOPIC = "emergency.assigned";
    public static final String EMERGENCY_COMPLETED_TOPIC = "emergency.completed";
    public static final String EMERGENCY_EXPIRED_TOPIC = "emergency.expired";
    public static final String PROVIDER_LOCATION_TOPIC = "provider.location.updated";
    public static final String PROVIDER_AVAILABILITY_TOPIC = "provider.availability.changed";

    // Default Values
    public static final int DEFAULT_SEARCH_RADIUS_KM = 5;
    public static final int MAX_SEARCH_RADIUS_KM = 50;
    public static final int RADIUS_INCREMENT_KM = 5;
    public static final int DEFAULT_REQUEST_EXPIRY_MINUTES = 15;
    public static final int DEFAULT_PROVIDER_TIMEOUT_SECONDS = 30;
    public static final int PROVIDER_LOCATION_TTL_MINUTES = 5;
    public static final int PROVIDER_AVAILABILITY_TTL_HOURS = 1;

    // Multipliers
    public static final double REPAIR_EMERGENCY_MULTIPLIER = 1.5;
    public static final double PETROL_EMERGENCY_MULTIPLIER = 1.2;

    // Error Messages
    public static final String ERROR_NO_PROVIDER_AVAILABLE = "No provider available in your area";
    public static final String ERROR_EMERGENCY_ALREADY_ASSIGNED = "Emergency already assigned to another provider";
    public static final String ERROR_EMERGENCY_EXPIRED = "Emergency request has expired";
    public static final String ERROR_INVALID_LOCATION = "Invalid location coordinates";
    public static final String ERROR_PROVIDER_NOT_AVAILABLE = "Provider is not available";

}
