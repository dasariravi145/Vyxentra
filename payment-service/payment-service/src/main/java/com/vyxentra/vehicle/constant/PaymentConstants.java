package com.vyxentra.vehicle.constant;



public final class PaymentConstants {

    private PaymentConstants() {}

    // ==================== Payment Status ====================

    public static final String STATUS_PENDING = "PENDING";
    public static final String STATUS_PROCESSING = "PROCESSING";
    public static final String STATUS_SUCCESS = "SUCCESS";
    public static final String STATUS_FAILED = "FAILED";
    public static final String STATUS_REFUNDED = "REFUNDED";
    public static final String STATUS_PARTIALLY_REFUNDED = "PARTIALLY_REFUNDED";

    // ==================== Payment Methods ====================

    public static final String METHOD_CARD = "CARD";
    public static final String METHOD_UPI = "UPI";
    public static final String METHOD_WALLET = "WALLET";
    public static final String METHOD_NETBANKING = "NETBANKING";
    public static final String METHOD_EMI = "EMI";

    // ==================== Payment Gateways ====================

    public static final String GATEWAY_RAZORPAY = "RAZORPAY";
    public static final String GATEWAY_STRIPE = "STRIPE";
    public static final String GATEWAY_PAYU = "PAYU";
    public static final String GATEWAY_CASHFREE = "CASHFREE";

    // ==================== Kafka Topics ====================

    public static final String PAYMENT_SUCCESS_TOPIC = "payment.success";
    public static final String PAYMENT_FAILED_TOPIC = "payment.failed";
    public static final String PAYMENT_CREATED_TOPIC = "payment.created";
    public static final String PAYMENT_PROCESSING_TOPIC = "payment.processing";

    public static final String REFUND_PROCESSED_TOPIC = "refund.processed";
    public static final String REFUND_FAILED_TOPIC = "refund.failed";

    public static final String PAYOUT_PROCESSED_TOPIC = "payout.processed";
    public static final String PAYOUT_CREATED_TOPIC = "payout.created";
    public static final String PAYOUT_PROCESSING_TOPIC = "payout.processing";
    public static final String PAYOUT_FAILED_TOPIC = "payout.failed";
    public static final String PAYOUT_RETRY_TOPIC = "payout.retry";
    public static final String PAYOUT_CANCELLED_TOPIC = "payout.cancelled";
    public static final String PAYOUT_SETTLED_TOPIC = "payout.settled";

    // ==================== Wallet Constants ====================

    public static final double MAX_WALLET_BALANCE = 50000.0;
    public static final double MIN_WALLET_TOPUP = 100.0;
    public static final int WALLET_TRANSACTION_EXPIRY_DAYS = 30;

    // ==================== Commission Constants ====================

    public static final double DEFAULT_COMMISSION_PERCENTAGE = 15.0;
    public static final double MIN_COMMISSION_AMOUNT = 10.0;

    // ==================== Redis Keys ====================

    public static final String PAYMENT_LOCK_PREFIX = "lock:payment:";
    public static final String PAYMENT_CACHE_PREFIX = "payment:";
    public static final String WALLET_CACHE_PREFIX = "wallet:";
    public static final String PAYOUT_CACHE_PREFIX = "payout:";

    // ==================== Error Messages ====================

    public static final String ERROR_INSUFFICIENT_BALANCE = "Insufficient wallet balance";
    public static final String ERROR_PAYMENT_NOT_FOUND = "Payment not found";
    public static final String ERROR_REFUND_FAILED = "Refund failed";
    public static final String ERROR_INVALID_AMOUNT = "Invalid amount";
    public static final String ERROR_PAYOUT_NOT_FOUND = "Payout not found";
    public static final String ERROR_PAYOUT_INVALID_STATUS = "Invalid payout status";
}
