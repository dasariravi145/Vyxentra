package com.vyxentra.vehicle.utils;



public final class PaymentConstants {

    private PaymentConstants() {}

    // Payment statuses
    public static final String STATUS_PENDING = "PENDING";
    public static final String STATUS_PROCESSING = "PROCESSING";
    public static final String STATUS_SUCCESS = "SUCCESS";
    public static final String STATUS_FAILED = "FAILED";
    public static final String STATUS_REFUNDED = "REFUNDED";
    public static final String STATUS_PARTIALLY_REFUNDED = "PARTIALLY_REFUNDED";

    // Payment methods
    public static final String METHOD_CARD = "CARD";
    public static final String METHOD_UPI = "UPI";
    public static final String METHOD_WALLET = "WALLET";
    public static final String METHOD_NETBANKING = "NETBANKING";
    public static final String METHOD_EMI = "EMI";

    // Payment gateways
    public static final String GATEWAY_RAZORPAY = "RAZORPAY";
    public static final String GATEWAY_STRIPE = "STRIPE";
    public static final String GATEWAY_PAYU = "PAYU";
    public static final String GATEWAY_CASHFREE = "CASHFREE";

    // Wallet constants
    public static final double MAX_WALLET_BALANCE = 50000.0;
    public static final double MIN_WALLET_TOPUP = 100.0;
    public static final int WALLET_TRANSACTION_EXPIRY_DAYS = 30;

    // Commission constants
    public static final double DEFAULT_COMMISSION_PERCENTAGE = 15.0;
    public static final double MIN_COMMISSION_AMOUNT = 10.0;

    // Redis keys
    public static final String PAYMENT_LOCK_PREFIX = "lock:payment:";
    public static final String PAYMENT_CACHE_PREFIX = "payment:";
    public static final String WALLET_CACHE_PREFIX = "wallet:";

    // Kafka topics
    public static final String PAYMENT_SUCCESS_TOPIC = "payment.success";
    public static final String PAYMENT_FAILED_TOPIC = "payment.failed";
    public static final String REFUND_PROCESSED_TOPIC = "refund.processed";
    public static final String PAYOUT_PROCESSED_TOPIC = "payout.processed";

    // Error messages
    public static final String ERROR_INSUFFICIENT_BALANCE = "Insufficient wallet balance";
    public static final String ERROR_PAYMENT_NOT_FOUND = "Payment not found";
    public static final String ERROR_REFUND_FAILED = "Refund failed";
    public static final String ERROR_INVALID_AMOUNT = "Invalid amount";
}
