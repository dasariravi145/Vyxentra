package com.vyxentra.vehicle.constants;

public final class ApiConstants {

    private ApiConstants() {}

    // API Base Paths
    public static final String API_BASE = "/api";
    public static final String API_VERSION = "/v1";
    public static final String API_BASE_V1 = API_BASE + API_VERSION;

    // Service specific base paths
    public static final String AUTH_SERVICE = API_BASE_V1 + "/auth";
    public static final String USER_SERVICE = API_BASE_V1 + "/users";
    public static final String PROVIDER_SERVICE = API_BASE_V1 + "/providers";
    public static final String EMPLOYEE_SERVICE = API_BASE_V1 + "/employees";
    public static final String BOOKING_SERVICE = API_BASE_V1 + "/bookings";
    public static final String PAYMENT_SERVICE = API_BASE_V1 + "/payments";
    public static final String NOTIFICATION_SERVICE = API_BASE_V1 + "/notifications";
    public static final String ADMIN_SERVICE = API_BASE_V1 + "/admin";

    // Common endpoints
    public static final String HEALTH = "/health";
    public static final String INFO = "/info";
    public static final String METRICS = "/metrics";

    // Pagination defaults
    public static final String DEFAULT_PAGE = "0";
    public static final String DEFAULT_SIZE = "20";
    public static final String DEFAULT_SORT = "createdAt,desc";

    // Date formats
    public static final String DATE_FORMAT = "yyyy-MM-dd";
    public static final String DATE_TIME_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'";
    public static final String TIME_FORMAT = "HH:mm:ss";

    // OTP
    public static final int OTP_LENGTH = 6;
    public static final int OTP_EXPIRY_MINUTES = 5;
    public static final int OTP_RESEND_COOLDOWN_SECONDS = 60;
    public static final int OTP_MAX_ATTEMPTS = 3;

    // Booking
    public static final int BOOKING_EXPIRY_MINUTES = 15;
    public static final int EMERGENCY_SEARCH_RADIUS = 5; // km
    public static final int EMERGENCY_MAX_RADIUS = 20; // km

    // Payment
    public static final double PLATFORM_COMMISSION_PERCENTAGE = 10.0;
    public static final double EMERGENCY_SURCHARGE_PERCENTAGE = 25.0;
    public static final double GST_PERCENTAGE = 18.0;
}
