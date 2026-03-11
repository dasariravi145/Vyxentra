package com.vyxentra.vehicle.constants;

public final class ServiceConstants {

    private ServiceConstants() {}

    // API Versions
    public static final String API_VERSION_V1 = "/api/v1";

    // Header Constants
    public static final String CORRELATION_ID_HEADER = "X-Correlation-ID";
    public static final String AUTHORIZATION_HEADER = "Authorization";
    public static final String USER_ID_HEADER = "X-User-ID";
    public static final String ROLE_HEADER = "X-Role";

    // Cache Keys
    public static final String OTP_CACHE_PREFIX = "otp:";
    public static final String PROVIDER_GEO_INDEX = "providers:geo";
    public static final String BIKE_REPAIR_INDEX = "bike:repair";
    public static final String CAR_REPAIR_INDEX = "car:repair";
    public static final String BIKE_FUEL_INDEX = "bike:fuel";
    public static final String CAR_FUEL_INDEX = "car:fuel";
    public static final String LOCK_KEY_PREFIX = "lock:";

    // OTP Configuration
    public static final int OTP_EXPIRY_SECONDS = 300; // 5 minutes
    public static final int OTP_LENGTH = 6;

    // Business Rules
    public static final double SERVICE_MULTIPLIER = 1.0;
    public static final double EMERGENCY_MULTIPLIER = 1.5;
    public static final double PETROL_MULTIPLIER = 1.2;
    public static final int MAX_RADIUS_KM = 50;
    public static final int INITIAL_RADIUS_KM = 5;
    public static final int RADIUS_INCREMENT_KM = 5;

    // Kafka Topics
    public static final String BOOKING_CREATED_TOPIC = "booking.created";
    public static final String EMERGENCY_TRIGGERED_TOPIC = "emergency.triggered";
    public static final String DAMAGE_REPORTED_TOPIC = "damage.reported";
    public static final String DAMAGE_APPROVED_TOPIC = "damage.approved";
    public static final String DAMAGE_REJECTED_TOPIC = "damage.rejected";
    public static final String REPAIR_DELAYED_TOPIC = "repair.delayed";
    public static final String SERVICE_STARTED_TOPIC = "service.started";
    public static final String SERVICE_COMPLETED_TOPIC = "service.completed";
    public static final String PAYMENT_SUCCESS_TOPIC = "payment.success";
    public static final String PROVIDER_APPROVED_TOPIC = "provider.approved";

    // Validation Messages
    public static final String INVALID_VEHICLE_TYPE = "Invalid vehicle type";
    public static final String PROVIDER_NOT_FOUND = "Provider not found";
    public static final String PROVIDER_SUSPENDED = "Provider is suspended";
    public static final String PROVIDER_NOT_APPROVED = "Provider not approved by admin";
    public static final String INVALID_PROVIDER_TYPE = "Provider does not support this service";
    public static final String BOOKING_NOT_FOUND = "Booking not found";
    public static final String INVALID_BOOKING_STATUS = "Invalid booking status for this operation";
    public static final String PAYMENT_REQUIRED = "Payment required before confirmation";
    public static final String APPROVAL_REQUIRED = "At least one item must be approved";
    public static final String UNAUTHORIZED_ACCESS = "Unauthorized access";
}