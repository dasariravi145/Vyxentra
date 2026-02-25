package com.vyxentra.vehicle.constants;

public final class ErrorCodes {

    private ErrorCodes() {}

    // Authentication errors (1000-1999)
    public static final String AUTH_INVALID_OTP = "AUTH_1001";
    public static final String AUTH_OTP_EXPIRED = "AUTH_1002";
    public static final String AUTH_MAX_ATTEMPTS = "AUTH_1003";
    public static final String AUTH_BLOCKED = "AUTH_1004";
    public static final String AUTH_INVALID_TOKEN = "AUTH_1005";
    public static final String AUTH_TOKEN_EXPIRED = "AUTH_1006";
    public static final String AUTH_UNAUTHORIZED = "AUTH_1007";
    public static final String AUTH_FORBIDDEN = "AUTH_1008";

    // User errors (2000-2999)
    public static final String USER_NOT_FOUND = "USER_2001";
    public static final String USER_ALREADY_EXISTS = "USER_2002";
    public static final String USER_INACTIVE = "USER_2003";
    public static final String USER_SUSPENDED = "USER_2004";

    // Provider errors (3000-3999)
    public static final String PROVIDER_NOT_FOUND = "PROV_3001";
    public static final String PROVIDER_ALREADY_EXISTS = "PROV_3002";
    public static final String PROVIDER_PENDING_APPROVAL = "PROV_3003";
    public static final String PROVIDER_SUSPENDED = "PROV_3004";
    public static final String PROVIDER_INACTIVE = "PROV_3005";
    public static final String PROVIDER_REJECTED = "PROV_3006";
    public static final String PROVIDER_UNAVAILABLE = "PROV_3007";

    // Booking errors (4000-4999)
    public static final String BOOKING_NOT_FOUND = "BOOK_4001";
    public static final String BOOKING_INVALID_STATE = "BOOK_4002";
    public static final String BOOKING_EXPIRED = "BOOK_4003";
    public static final String BOOKING_ALREADY_ASSIGNED = "BOOK_4004";
    public static final String BOOKING_CANCELLATION_NOT_ALLOWED = "BOOK_4005";
    public static final String BOOKING_PAYMENT_REQUIRED = "BOOK_4006";
    public static final String BOOKING_DAMAGE_REPORT_REQUIRED = "BOOK_4007";
    public static final String BOOKING_APPROVAL_REQUIRED = "BOOK_4008";

    // Payment errors (5000-5999)
    public static final String PAYMENT_NOT_FOUND = "PAY_5001";
    public static final String PAYMENT_FAILED = "PAY_5002";
    public static final String PAYMENT_ALREADY_PROCESSED = "PAY_5003";
    public static final String PAYMENT_INSUFFICIENT_FUNDS = "PAY_5004";
    public static final String PAYMENT_INVALID_AMOUNT = "PAY_5005";

    // Validation errors (6000-6999)
    public static final String VALIDATION_FAILED = "VAL_6001";
    public static final String VALIDATION_INVALID_INPUT = "VAL_6002";
    public static final String VALIDATION_MISSING_FIELD = "VAL_6003";

    // Business rule violations (7000-7999)
    public static final String RULE_VIOLATION = "RULE_7001";
    public static final String RULE_PROVIDER_NOT_APPROVED = "RULE_7002";
    public static final String RULE_PROVIDER_NOT_SUPPORT_VEHICLE = "RULE_7003";
    public static final String RULE_EMERGENCY_NOT_SUPPORTED = "RULE_7004";
    public static final String RULE_DAMAGE_ALREADY_REPORTED = "RULE_7005";
    public static final String RULE_NO_ITEMS_APPROVED = "RULE_7006";

    // System errors (9000-9999)
    public static final String SYSTEM_ERROR = "SYS_9001";
    public static final String SYSTEM_LOCK_ACQUISITION_FAILED = "SYS_9002";
    public static final String SYSTEM_CIRCUIT_BREAKER_OPEN = "SYS_9003";
    public static final String SYSTEM_DEPENDENCY_UNAVAILABLE = "SYS_9004";
}
