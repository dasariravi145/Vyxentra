package com.vyxentra.vehicle.constants;

import com.vyxentra.vehicle.enums.VehicleType;

public final class RedisKeys {

    private RedisKeys() {}

    // OTP keys
    public static final String OTP_PREFIX = "otp:";
    public static final String OTP_ATTEMPT_PREFIX = "otp:attempt:";
    public static final String OTP_BLOCK_PREFIX = "otp:block:";

    // Geo keys
    public static final String GEO_PROVIDER_BIKE = "geo:provider:bike";
    public static final String GEO_PROVIDER_CAR = "geo:provider:car";
    public static final String GEO_PROVIDER_EMERGENCY_BIKE = "geo:emergency:bike";
    public static final String GEO_PROVIDER_EMERGENCY_CAR = "geo:emergency:car";

    // Lock keys
    public static final String LOCK_BOOKING_PREFIX = "lock:booking:";
    public static final String LOCK_PROVIDER_PREFIX = "lock:provider:";
    public static final String LOCK_EMERGENCY_PREFIX = "lock:emergency:";
    public static final String LOCK_PAYMENT_PREFIX = "lock:payment:";

    // Cache keys
    public static final String CACHE_PROVIDER_PREFIX = "cache:provider:";
    public static final String CACHE_USER_PREFIX = "cache:user:";
    public static final String CACHE_BOOKING_PREFIX = "cache:booking:";
    public static final String CACHE_SERVICE_CATALOG_PREFIX = "cache:service:catalog:";

    // Tracking keys
    public static final String TRACKING_BOOKING_PREFIX = "tracking:booking:";
    public static final String TRACKING_PROVIDER_LOCATION_PREFIX = "tracking:provider:location:";
    public static final String TRACKING_EMERGENCY_BROADCAST_PREFIX = "tracking:emergency:broadcast:";

    // Session keys
    public static final String SESSION_USER_PREFIX = "session:user:";
    public static final String SESSION_TOKEN_PREFIX = "session:token:";

    // Rate limiting
    public static final String RATE_LIMIT_PREFIX = "ratelimit:";

    public static String getOtpKey(String mobileNumber) {
        return OTP_PREFIX + mobileNumber;
    }

    public static String getOtpAttemptKey(String mobileNumber) {
        return OTP_ATTEMPT_PREFIX + mobileNumber;
    }

    public static String getOtpBlockKey(String mobileNumber) {
        return OTP_BLOCK_PREFIX + mobileNumber;
    }

    public static String getBookingLockKey(String bookingId) {
        return LOCK_BOOKING_PREFIX + bookingId;
    }

    public static String getProviderLockKey(String providerId) {
        return LOCK_PROVIDER_PREFIX + providerId;
    }

    public static String getEmergencyLockKey(String emergencyId) {
        return LOCK_EMERGENCY_PREFIX + emergencyId;
    }

    public static String getProviderCacheKey(String providerId) {
        return CACHE_PROVIDER_PREFIX + providerId;
    }

    public static String getUserCacheKey(String userId) {
        return CACHE_USER_PREFIX + userId;
    }

    public static String getBookingCacheKey(String bookingId) {
        return CACHE_BOOKING_PREFIX + bookingId;
    }

    public static String getTrackingKey(String bookingId) {
        return TRACKING_BOOKING_PREFIX + bookingId;
    }

    public static String getGeoKey(VehicleType vehicleType, boolean emergency) {
        if (emergency) {
            return vehicleType == VehicleType.BIKE ? GEO_PROVIDER_EMERGENCY_BIKE : GEO_PROVIDER_EMERGENCY_CAR;
        }
        return vehicleType == VehicleType.BIKE ? GEO_PROVIDER_BIKE : GEO_PROVIDER_CAR;
    }
}
