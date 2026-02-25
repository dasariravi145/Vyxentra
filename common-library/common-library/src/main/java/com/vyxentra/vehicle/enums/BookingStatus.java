package com.vyxentra.vehicle.enums;

public enum BookingStatus {

    // Common
    PENDING_ASSIGNMENT,
    ASSIGNED,
    CANCELLED,
    REJECTED,
    EXPIRED,

    // Service Center flow
    DAMAGE_REPORTED,
    AWAITING_CUSTOMER_APPROVAL,
    DAMAGE_APPROVED,
    DAMAGE_REJECTED,
    REPAIR_IN_PROGRESS,
    REPAIR_COMPLETED,

    // Washing Center flow
    WASH_IN_PROGRESS,
    WASH_COMPLETED,

    // Final states
    COMPLETED,
    PAYMENT_PENDING,
    PAYMENT_COMPLETED

}
