package com.vyxentra.vehicle.repository;

import com.vyxentra.vehicle.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, String> {

    Optional<Payment> findByPaymentNumber(String paymentNumber);

    List<Payment> findByBookingIdOrderByCreatedAtDesc(String bookingId);

    List<Payment> findByCustomerIdOrderByCreatedAtDesc(String customerId);

    @Query("SELECT p FROM Payment p WHERE p.customerId = :customerId " +
            "AND (:fromDate IS NULL OR p.createdAt >= :fromDate) " +
            "AND (:toDate IS NULL OR p.createdAt <= :toDate) " +
            "ORDER BY p.createdAt DESC")
    List<Payment> findCustomerPayments(@Param("customerId") String customerId,
                                       @Param("fromDate") LocalDateTime fromDate,
                                       @Param("toDate") LocalDateTime toDate);

    @Query("SELECT p FROM Payment p WHERE p.bookingId = :bookingId AND p.status = 'SUCCESS'")
    Optional<Payment> findSuccessfulPaymentByBookingId(@Param("bookingId") String bookingId);

    @Query("SELECT p FROM Payment p WHERE p.status = 'PENDING' AND p.createdAt < :timeout")
    List<Payment> findExpiredPendingPayments(@Param("timeout") LocalDateTime timeout);

    @Query("SELECT p FROM Payment p WHERE p.gatewayPaymentId = :gatewayPaymentId")
    Optional<Payment> findByGatewayPaymentId(@Param("gatewayPaymentId") String gatewayPaymentId);

    @Query("SELECT p FROM Payment p WHERE p.gatewayOrderId = :gatewayOrderId")
    Optional<Payment> findByGatewayOrderId(@Param("gatewayOrderId") String gatewayOrderId);

    @Modifying
    @Query("UPDATE Payment p SET p.status = :status, p.errorMessage = :errorMessage " +
            "WHERE p.id = :paymentId")
    void updateStatus(@Param("paymentId") String paymentId,
                      @Param("status") String status,
                      @Param("errorMessage") String errorMessage);

    @Modifying
    @Query("UPDATE Payment p SET p.retryCount = p.retryCount + 1 WHERE p.id = :paymentId")
    void incrementRetryCount(@Param("paymentId") String paymentId);
}
