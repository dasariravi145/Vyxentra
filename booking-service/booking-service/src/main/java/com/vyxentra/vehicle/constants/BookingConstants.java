package com.vyxentra.vehicle.constants;



public final class BookingConstants {

    private BookingConstants() {}

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

    // Cache Keys
    public static final String BOOKING_CACHE_PREFIX = "booking:";
    public static final String DAMAGE_CACHE_PREFIX = "damage:";
    public static final String LOCK_KEY_PREFIX = "lock:booking:";

    // Error Messages
    public static final String BOOKING_NOT_FOUND = "Booking not found";
    public static final String DAMAGE_REPORT_NOT_FOUND = "Damage report not found";
    public static final String INVALID_BOOKING_STATUS = "Invalid booking status for this operation";
    public static final String PAYMENT_REQUIRED = "Payment required before confirmation";
    public static final String APPROVAL_ITEMS_REQUIRED = "At least one damage item must be approved";
}
