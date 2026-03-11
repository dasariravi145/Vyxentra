package com.vyxentra.vehicle.repository;


import com.vyxentra.vehicle.entity.DamageReport;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface DamageReportRepository extends JpaRepository<DamageReport, String> {

    List<DamageReport> findByBookingIdOrderByCreatedAtDesc(String bookingId);

    List<DamageReport> findByBookingIdAndStatus(String bookingId, String status);

    @Query("SELECT dr FROM DamageReport dr WHERE dr.booking.customerId = :customerId " +
            "AND dr.status = 'REPORTED'")
    List<DamageReport> findPendingForCustomer(@Param("customerId") String customerId);

    @Query("SELECT dr FROM DamageReport dr WHERE dr.createdAt < :timeout " +
            "AND dr.status = 'REPORTED'")
    List<DamageReport> findExpiredReports(@Param("timeout") LocalDateTime timeout);

    @Modifying
    @Query("UPDATE DamageReport dr SET dr.status = :status, dr.approvedAmount = :amount, " +
            "dr.approvedBy = :approvedBy, dr.approvedAt = :approvedAt WHERE dr.id = :reportId")
    void updateApproval(@Param("reportId") String reportId,
                        @Param("status") String status,
                        @Param("amount") Double amount,
                        @Param("approvedBy") String approvedBy,
                        @Param("approvedAt") LocalDateTime approvedAt);

    @Modifying
    @Query("UPDATE DamageReport dr SET dr.status = 'REJECTED', dr.rejectionReason = :reason, " +
            "dr.approvedBy = :approvedBy, dr.approvedAt = :approvedAt WHERE dr.id = :reportId")
    void rejectReport(@Param("reportId") String reportId,
                      @Param("reason") String reason,
                      @Param("approvedBy") String approvedBy,
                      @Param("approvedAt") LocalDateTime approvedAt);
}
