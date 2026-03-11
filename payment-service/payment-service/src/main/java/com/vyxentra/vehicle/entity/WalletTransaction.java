package com.vyxentra.vehicle.entity;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "wallet_transactions")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class WalletTransaction {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "wallet_id", nullable = false)
    private Wallet wallet;

    @Column(name = "transaction_number", nullable = false, unique = true)
    private String transactionNumber;

    @Column(nullable = false)
    private String type; // CREDIT, DEBIT

    @Column(nullable = false)
    private Double amount;

    @Column(name = "balance_after", nullable = false)
    private Double balanceAfter;

    @Column(name = "reference_id")
    private String referenceId; // payment_id, booking_id, refund_id

    @Column(name = "reference_type")
    private String referenceType; // PAYMENT, BOOKING, REFUND, TOPUP

    @Column(length = 500)
    private String description;

    @Column(nullable = false)
    private String status; // SUCCESS, FAILED, PENDING

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
