package com.vyxentra.vehicle.constants;

public final class KafkaTopics {

    private KafkaTopics() {}

    // Booking events
    public static final String BOOKING_EVENTS = "booking-events";
    public static final String BOOKING_CREATED = "booking-created";
    public static final String BOOKING_ASSIGNED = "booking-assigned";
    public static final String BOOKING_CANCELLED = "booking-cancelled";
    public static final String BOOKING_COMPLETED = "booking-completed";

    // Emergency events
    public static final String EMERGENCY_EVENTS = "emergency-events";
    public static final String EMERGENCY_TRIGGERED = "emergency-triggered";
    public static final String EMERGENCY_ASSIGNED = "emergency-assigned";
    public static final String EMERGENCY_RESOLVED = "emergency-resolved";

    // Damage/Repair events
    public static final String DAMAGE_EVENTS = "damage-events";
    public static final String DAMAGE_REPORTED = "damage-reported";
    public static final String DAMAGE_APPROVED = "damage-approved";
    public static final String DAMAGE_REJECTED = "damage-rejected";
    public static final String REPAIR_DELAYED = "repair-delayed";

    // Service events
    public static final String SERVICE_EVENTS = "service-events";
    public static final String SERVICE_STARTED = "service-started";
    public static final String SERVICE_COMPLETED = "service-completed";

    // Payment events
    public static final String PAYMENT_EVENTS = "payment-events";
    public static final String PAYMENT_INITIATED = "payment-initiated";
    public static final String PAYMENT_SUCCESS = "payment-success";
    public static final String PAYMENT_FAILED = "payment-failed";
    public static final String PAYMENT_REFUNDED = "payment-refunded";

    // Provider events
    public static final String PROVIDER_EVENTS = "provider-events";
    public static final String PROVIDER_APPROVED = "provider-approved";
    public static final String PROVIDER_SUSPENDED = "provider-suspended";
    public static final String PROVIDER_ACTIVATED = "provider-activated";

    // Notification events
    public static final String NOTIFICATION_EVENTS = "notification-events";
    public static final String EMAIL_NOTIFICATIONS = "email-notifications";
    public static final String SMS_NOTIFICATIONS = "sms-notifications";
    public static final String PUSH_NOTIFICATIONS = "push-notifications";

    // Dead letter queue
    public static final String DLQ_PREFIX = "dlq-";

    public static String getDlqTopic(String originalTopic) {
        return DLQ_PREFIX + originalTopic;
    }
}
