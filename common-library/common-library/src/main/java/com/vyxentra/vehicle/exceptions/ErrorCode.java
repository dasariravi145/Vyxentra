package com.vyxentra.vehicle.exceptions;



import lombok.Getter;

@Getter
public enum ErrorCode {
    // General Errors (1000-1999)
    INTERNAL_SERVER_ERROR("VEH-1000", "Internal server error"),
    VALIDATION_ERROR("VEH-1001", "Validation failed"),
    UNAUTHORIZED("VEH-1002", "Unauthorized access"),
    FORBIDDEN("VEH-1003", "Access forbidden"),
    RESOURCE_NOT_FOUND("VEH-1004", "Resource not found"),
    BAD_REQUEST("VEH-1005", "Bad request"),

    // User Errors (2000-2999)
    USER_NOT_FOUND("VEH-2000", "User not found"),
    USER_ALREADY_EXISTS("VEH-2001", "User already exists"),
    INVALID_CREDENTIALS("VEH-2002", "Invalid credentials"),
    OTP_INVALID("VEH-2003", "Invalid OTP"),
    OTP_EXPIRED("VEH-2004", "OTP expired"),
    OTP_MAX_ATTEMPTS("VEH-2005", "Maximum OTP verification attempts exceeded"),

    // Provider Errors (3000-3999)
    PROVIDER_NOT_FOUND("VEH-3000", "Provider not found"),
    PROVIDER_SUSPENDED("VEH-3001", "Provider is suspended"),
    PROVIDER_NOT_APPROVED("VEH-3002", "Provider not approved by admin"),
    PROVIDER_ALREADY_EXISTS("VEH-3003", "Provider already exists"),
    PROVIDER_INVALID_TYPE("VEH-3004", "Invalid provider type"),
    PROVIDER_INVALID_VEHICLE_SUPPORT("VEH-3005", "Provider must support at least one vehicle type"),
    CUSTOMER_ALREADY_HAS_EMERGENCY("EMG-7011", "Customer already has an active emergency request"),
    QUANTITY_EXCEEDS_LIMIT("EMG-7018", "Requested quantity exceeds maximum limit"),

    // Booking Errors (4000-4999)
    BOOKING_NOT_FOUND("VEH-4000", "Booking not found"),
    BOOKING_INVALID_STATUS("VEH-4001", "Invalid booking status"),
    BOOKING_PAYMENT_REQUIRED("VEH-4002", "Payment required for this booking type"),
    BOOKING_ALREADY_ASSIGNED("VEH-4003", "Booking already assigned to another provider"),
    BOOKING_EXPIRED("VEH-4004", "Booking has expired"),
    BOOKING_NOT_ASSIGNABLE("VEH-4005", "Booking cannot be assigned"),

    // Payment Errors (5000-5999)
    PAYMENT_FAILED("VEH-5000", "Payment failed"),
    PAYMENT_NOT_FOUND("VEH-5001", "Payment not found"),
    PAYMENT_ALREADY_PROCESSED("VEH-5002", "Payment already processed"),
    INSUFFICIENT_BALANCE("VEH-5003", "Insufficient balance"),

    // Damage/Approval Errors (6000-6999)
    DAMAGE_REPORT_NOT_FOUND("VEH-6000", "Damage report not found"),
    DAMAGE_ALREADY_APPROVED("VEH-6001", "Damage items already approved"),
    DAMAGE_ALREADY_REJECTED("VEH-6002", "Damage items already rejected"),
    APPROVAL_ITEMS_REQUIRED("VEH-6003", "At least one damage item must be approved"),
    DAMAGE_REPORT_EXPIRED("VEH-6004", "Damage report approval expired"),

    // Emergency Errors (7000-7999)
    EMERGENCY_NOT_FOUND("VEH-7000", "Emergency not found"),
    EMERGENCY_ALREADY_ASSIGNED("VEH-7001", "Emergency already assigned"),
    EMERGENCY_EXPIRED("VEH-7002", "Emergency request expired"),
    NO_PROVIDER_AVAILABLE("VEH-7003", "No provider available in your area"),
    EMERGENCY_PAYMENT_REQUIRED("VEH-7004", "Upfront payment required for emergency"),
    EMERGENCY_INVALID_STATUS("EMG-7006", "Invalid emergency status for this operation"),



    // Lock Errors (8000-8999)
    LOCK_ACQUISITION_FAILED("VEH-8000", "Failed to acquire lock"),
    LOCK_RELEASE_FAILED("VEH-8001", "Failed to release lock"),
    CONCURRENT_MODIFICATION("VEH-8002", "Concurrent modification detected"),

    // Catalog Errors (4000-4999)
    SERVICE_NOT_FOUND("CAT-4001", "Service not found"),
    CATEGORY_NOT_FOUND("CAT-4002", "Category not found"),
    ADDON_NOT_FOUND("CAT-4003", "Addon not found"),
    SERVICE_ALREADY_EXISTS("CAT-4004", "Service already exists"),
    CATEGORY_ALREADY_EXISTS("CAT-4005", "Category already exists"),
    ADDON_ALREADY_EXISTS("CAT-4006", "Addon already exists"),
    INVALID_SERVICE_TYPE("CAT-4007", "Invalid service type"),
    INVALID_VEHICLE_TYPE("CAT-4008", "Invalid vehicle type"),
    INVALID_PROVIDER_TYPE("CAT-4009", "Invalid provider type"),
    PRICE_REQUIRED("CAT-4010", "Price is required for at least one vehicle type"),
    DURATION_INVALID("CAT-4011", "Estimated duration must be positive"),
    DISPLAY_ORDER_INVALID("CAT-4012", "Display order must be non-negative"),
    CATEGORY_HAS_SERVICES("CAT-4013", "Cannot delete category with existing services"),
    INVALID_LOCATION("EMG-7013", "Invalid location coordinates provided"),
    PROVIDER_NOT_AVAILABLE("EMG-7021", "Provider is not available at the moment"),
    PROVIDER_NOT_ELIGIBLE("EMG-7009", "Provider not eligible for this emergency type"),
    SERVICE_HAS_ADDONS("CAT-4014", "Cannot delete service with existing addons"),

    LOCATION_UPDATE_FAILED("TRK-5004", "Failed to update location"),
    TRACKING_SESSION_NOT_FOUND("TRK-5005", "Tracking session not found"),
    INVALID_COORDINATES("TRK-5006", "Invalid coordinates provided"),
    ETA_CALCULATION_FAILED("TRK-5007", "Failed to calculate ETA"),
    TRACKING_SESSION_ALREADY_EXISTS("TRK-5008", "Tracking session already exists for this booking"),
    TRACKING_SESSION_EXPIRED("TRK-5009", "Tracking session has expired"),
    TRACKING_SESSION_COMPLETED("TRK-5010", "Tracking session already completed"),
    TRACKING_SESSION_PAUSED("TRK-5011", "Tracking session is paused"),
    NO_LOCATION_DATA("TRK-5012", "No location data available for this entity"),
    RATE_LIMIT_EXCEEDED("TRK-5013", "Too many location updates. Please slow down."),
    WEBSOCKET_CONNECTION_NOT_FOUND("TRK-5014", "WebSocket connection not found"),
    LOCATION_UPDATE_TOO_FREQUENT("TRK-5015", "Location updates too frequent. Minimum interval: 1 second"),
    WEBSOCKET_CONNECTION_ALREADY_EXISTS("TRK-5016", "WebSocket connection already exists"),
    PAYMENT_INVALID_STATUS("TRK-5016","Payment Invalid Status"),
    EMAIL_SEND_FAILED("TRK-5017","Email Send Failed"),
    SMS_SEND_FAILED("TRK-5018","SMS Send Failed"),
    PUSH_SEND_FAILED("TRK-5019","Push Send Failed"),
    INVALID_EMAIL_TEMPLATE("TRK-5020","Invalid Email Template"),
    DEVICE_NOT_FOUND("TRK-5021","Device Not Found"),
    PROVIDER_UNAVAILABLE("TRK-5022","Provider Un Available"),
    MAX_DEVICES_EXCEEDED("TRK-5023","Max Devices Exceeded"),
    PAYMENT_GATEWAY_NOT_FOUND("TRK-5024","Payment Gateway Not Found");




    private final String code;
    private final String message;

    ErrorCode(String code, String message) {
        this.code = code;
        this.message = message;
    }
}
