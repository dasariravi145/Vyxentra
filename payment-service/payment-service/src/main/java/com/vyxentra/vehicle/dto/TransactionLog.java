package com.vyxentra.vehicle.dto;

import com.vyxentra.vehicle.entity.Payment;
import com.vyxentra.vehicle.entity.Payout;
import com.vyxentra.vehicle.entity.Refund;
import com.vyxentra.vehicle.entity.WalletTransaction;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;
import java.util.Map;

@Entity
@Table(name = "transaction_logs", indexes = {
        @Index(name = "idx_txn_transaction", columnList = "transaction_id"),
        @Index(name = "idx_txn_type", columnList = "transaction_type"),
        @Index(name = "idx_txn_reference", columnList = "reference_id"),
        @Index(name = "idx_txn_user", columnList = "user_id"),
        @Index(name = "idx_txn_status", columnList = "status"),
        @Index(name = "idx_txn_created", columnList = "created_at")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class TransactionLog {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    /**
     * Unique transaction identifier (can be payment ID, refund ID, etc.)
     */
    @Column(name = "transaction_id", nullable = false)
    private String transactionId;

    /**
     * Type of transaction: PAYMENT, REFUND, WALLET_TOPUP, WALLET_DEBIT, PAYOUT
     */
    @Column(name = "transaction_type", nullable = false)
    private String transactionType;

    /**
     * Action performed: CREATED, PROCESSED, SUCCESS, FAILED, REFUNDED, CANCELLED
     */
    @Column(nullable = false)
    private String action;

    /**
     * User ID who performed the action
     */
    @Column(name = "user_id")
    private String userId;

    /**
     * User type: CUSTOMER, PROVIDER, ADMIN, SYSTEM
     */
    @Column(name = "user_type")
    private String userType;

    /**
     * Amount involved in the transaction
     */
    private Double amount;

    /**
     * Currency of the transaction
     */
    private String currency;

    /**
     * Status of the transaction: PENDING, SUCCESS, FAILED
     */
    private String status;

    /**
     * Reference ID (booking ID, payment ID, refund ID, etc.)
     */
    @Column(name = "reference_id")
    private String referenceId;

    /**
     * Reference type: BOOKING, PAYMENT, REFUND, WALLET, PAYOUT
     */
    @Column(name = "reference_type")
    private String referenceType;

    /**
     * Payment gateway used: RAZORPAY, STRIPE, PAYU, etc.
     */
    @Column(name = "payment_gateway")
    private String paymentGateway;

    /**
     * Gateway transaction ID
     */
    @Column(name = "gateway_transaction_id")
    private String gatewayTransactionId;

    /**
     * IP address of the client
     */
    @Column(name = "ip_address", length = 45)
    private String ipAddress;

    /**
     * User agent of the client
     */
    @Column(name = "user_agent", length = 500)
    private String userAgent;

    /**
     * Device ID if available
     */
    @Column(name = "device_id")
    private String deviceId;

    /**
     * Request payload sent to gateway
     */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "request_payload", columnDefinition = "jsonb")
    private Map<String, Object> requestPayload;

    /**
     * Response payload received from gateway
     */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "response_payload", columnDefinition = "jsonb")
    private Map<String, Object> responsePayload;

    /**
     * Additional metadata for the transaction
     */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private Map<String, Object> metadata;

    /**
     * HTTP method for API calls
     */
    @Column(name = "http_method")
    private String httpMethod;

    /**
     * HTTP path for API calls
     */
    @Column(name = "http_path", length = 500)
    private String httpPath;

    /**
     * HTTP status code
     */
    @Column(name = "http_status")
    private Integer httpStatus;

    /**
     * Duration of the transaction in milliseconds
     */
    @Column(name = "duration_ms")
    private Long durationMs;

    /**
     * Error message if transaction failed
     */
    @Column(name = "error_message", length = 1000)
    private String errorMessage;

    /**
     * Error code if transaction failed
     */
    @Column(name = "error_code")
    private String errorCode;

    /**
     * Stack trace for debugging (truncated)
     */
    @Column(name = "stack_trace", length = 4000)
    private String stackTrace;

    /**
     * Whether the transaction was successful
     */
    private Boolean success;

    /**
     * Source of the transaction: API, WEBHOOK, SCHEDULER, ADMIN
     */
    private String source;

    /**
     * Environment: DEV, TEST, PROD
     */
    private String environment;

    /**
     * Version of the API
     */
    @Column(name = "api_version")
    private String apiVersion;

    /**
     * Created by user ID
     */
    @Column(name = "created_by")
    private String createdBy;

    /**
     * Created at timestamp
     */
    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    // ==================== Helper Methods ====================

    @PrePersist
    protected void onCreate() {
        if (environment == null) {
            environment = "PROD";
        }
        if (source == null) {
            source = "API";
        }
    }

    /**
     * Builder with convenience methods
     */
    public static class TransactionLogBuilder {

        public TransactionLogBuilder fromPayment(Payment payment, String action) {
            this.transactionId = payment.getId();
            this.transactionType = "PAYMENT";
            this.action = action;
            this.amount = payment.getAmount();
            this.currency = payment.getCurrency();
            this.status = payment.getStatus();
            this.referenceId = payment.getBookingId();
            this.referenceType = "BOOKING";
            this.paymentGateway = payment.getPaymentGateway();
            this.gatewayTransactionId = payment.getGatewayPaymentId();
            this.userId = payment.getCustomerId();
            this.userType = "CUSTOMER";
            return this;
        }

        public TransactionLogBuilder fromRefund(Refund refund, String action) {
            this.transactionId = refund.getId();
            this.transactionType = "REFUND";
            this.action = action;
            this.amount = refund.getAmount();
            this.currency = "INR";
            this.status = refund.getStatus();
            this.referenceId = refund.getPayment().getId();
            this.referenceType = "PAYMENT";
            this.userId = refund.getPayment().getCustomerId();
            this.userType = "CUSTOMER";
            return this;
        }

        public TransactionLogBuilder fromWallet(WalletTransaction walletTxn, String action) {
            this.transactionId = walletTxn.getId();
            this.transactionType = "WALLET_" + walletTxn.getType();
            this.action = action;
            this.amount = walletTxn.getAmount();
            this.currency = "INR";
            this.status = walletTxn.getStatus();
            this.referenceId = walletTxn.getReferenceId();
            this.referenceType = walletTxn.getReferenceType();
            this.userId = walletTxn.getWallet().getUserId();
            this.userType = walletTxn.getWallet().getUserType();
            return this;
        }

        public TransactionLogBuilder fromPayout(Payout payout, String action) {
            this.transactionId = payout.getId();
            this.transactionType = "PAYOUT";
            this.action = action;
            this.amount = payout.getAmount();
            this.currency = "INR";
            this.status = payout.getStatus();
            this.referenceId = payout.getProviderId();
            this.referenceType = "PROVIDER";
            this.userId = payout.getProviderId();
            this.userType = "PROVIDER";
            return this;
        }

        public TransactionLogBuilder withRequest(Map<String, Object> request) {
            this.requestPayload = request;
            return this;
        }

        public TransactionLogBuilder withResponse(Map<String, Object> response) {
            this.responsePayload = response;
            return this;
        }

        public TransactionLogBuilder withSuccess(boolean success) {
            this.success = success;
            return this;
        }

        public TransactionLogBuilder withError(String errorMessage, String errorCode) {
            this.errorMessage = errorMessage;
            this.errorCode = errorCode;
            this.success = false;
            return this;
        }

        public TransactionLogBuilder withHttpDetails(String method, String path, Integer status, Long duration) {
            this.httpMethod = method;
            this.httpPath = path;
            this.httpStatus = status;
            this.durationMs = duration;
            return this;
        }

        public TransactionLogBuilder withClientInfo(String ip, String userAgent, String deviceId) {
            this.ipAddress = ip;
            this.userAgent = userAgent;
            this.deviceId = deviceId;
            return this;
        }

        public TransactionLogBuilder withMetadata(Map<String, Object> metadata) {
            this.metadata = metadata;
            return this;
        }
    }
}
