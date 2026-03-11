package com.vyxentra.vehicle.repository;

import com.vyxentra.vehicle.entity.Refund;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RefundRepository extends JpaRepository<Refund, String> {

    Optional<Refund> findByRefundNumber(String refundNumber);

    List<Refund> findByPaymentIdOrderByCreatedAtDesc(String paymentId);

    List<Refund> findByBookingIdOrderByCreatedAtDesc(String bookingId);

    @Query("SELECT r FROM Refund r WHERE r.status = 'PENDING' " +
            "AND (:paymentId IS NULL OR r.payment.id = :paymentId)")
    List<Refund> findPendingRefunds(@Param("paymentId") String paymentId);

    @Query("SELECT SUM(r.amount) FROM Refund r WHERE r.payment.id = :paymentId AND r.status = 'SUCCESS'")
    Double getTotalRefundedAmount(@Param("paymentId") String paymentId);
    Optional<Refund> findByGatewayRefundId(String gatewayRefundId);
}
