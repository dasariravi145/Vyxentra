package com.vyxentra.vehicle.util;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;

public final class IdGenerator {

    private static final AtomicLong counter = new AtomicLong(1000);
    private static final String INSTANCE_ID = generateInstanceId();

    private IdGenerator() {}

    /**
     * Generate a UUID
     */
    public static String generateUUID() {
        return UUID.randomUUID().toString();
    }

    /**
     * Generate a short UUID (first 8 characters)
     */
    public static String generateShortId() {
        return UUID.randomUUID().toString().substring(0, 8);
    }

    /**
     * Generate a booking reference (format: BK-YYYYMMDD-XXXXX)
     */
    public static String generateBookingReference() {
        String date = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String sequence = String.format("%05d", counter.incrementAndGet() % 100000);
        return "BK-" + date + "-" + sequence;
    }

    /**
     * Generate a payment reference (format: PY-YYYYMMDD-XXXXX)
     */
    public static String generatePaymentReference() {
        String date = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String sequence = String.format("%05d", counter.incrementAndGet() % 100000);
        return "PY-" + date + "-" + sequence;
    }

    /**
     * Generate an OTP
     */
    public static String generateOTP() {
        return String.format("%06d", (int)(Math.random() * 1000000));
    }

    /**
     * Generate a transaction ID
     */
    public static String generateTransactionId() {
        return "TXN" + Instant.now().toEpochMilli() + counter.incrementAndGet();
    }

    /**
     * Generate a correlation ID for tracing
     */
    public static String generateCorrelationId() {
        return INSTANCE_ID + "-" + System.currentTimeMillis() + "-" + counter.incrementAndGet();
    }

    /**
     * Generate a request ID
     */
    public static String generateRequestId() {
        return "REQ-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    /**
     * Generate an event ID
     */
    public static String generateEventId() {
        return "EVT-" + System.currentTimeMillis() + "-" + counter.incrementAndGet();
    }

    /**
     * Generate a damage report ID
     */
    public static String generateDamageReportId() {
        return "DAM-" + System.currentTimeMillis() + "-" + counter.incrementAndGet();
    }

    /**
     * Generate an approval ID
     */
    public static String generateApprovalId() {
        return "APR-" + System.currentTimeMillis() + "-" + counter.incrementAndGet();
    }

    private static String generateInstanceId() {
        String hostname = System.getenv("HOSTNAME");
        if (hostname != null && hostname.length() > 8) {
            return hostname.substring(0, 8);
        }
        return UUID.randomUUID().toString().substring(0, 8);
    }
}
