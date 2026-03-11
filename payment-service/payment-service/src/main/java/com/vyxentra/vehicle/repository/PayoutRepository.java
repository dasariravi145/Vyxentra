package com.vyxentra.vehicle.repository;

import com.vyxentra.vehicle.entity.Payout;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface PayoutRepository extends JpaRepository<Payout, String> {

    Optional<Payout> findByPayoutNumber(String payoutNumber);

    Page<Payout> findByProviderIdOrderByCreatedAtDesc(String providerId, Pageable pageable);

    List<Payout> findByProviderIdAndStatus(String providerId, String status);

    @Query("SELECT p FROM Payout p WHERE p.providerId = :providerId AND p.periodStart >= :fromDate AND p.periodEnd <= :toDate")
    Page<Payout> findByProviderIdAndPeriodBetween(@Param("providerId") String providerId,
                                                  @Param("fromDate") LocalDate fromDate,
                                                  @Param("toDate") LocalDate toDate,
                                                  Pageable pageable);

    @Query("SELECT p FROM Payout p WHERE p.status = 'PENDING' AND p.periodEnd <= :settlementDate")
    List<Payout> findPendingPayouts(@Param("settlementDate") LocalDate settlementDate);

    @Query("SELECT p FROM Payout p WHERE p.status IN ('PENDING', 'PROCESSING')")
    List<Payout> findUnprocessedPayouts();

    @Query("SELECT p FROM Payout p WHERE p.settlementStatus != 'SETTLED' OR p.settlementStatus IS NULL")
    List<Payout> findBySettlementStatusNot(@Param("status") String status);

    long countByStatus(String status);

    boolean existsByProviderIdAndPeriodStartAndPeriodEnd(String providerId, LocalDate periodStart, LocalDate periodEnd);

    @Query("SELECT COALESCE(SUM(p.netAmount), 0) FROM Payout p WHERE p.providerId = :providerId AND p.status = 'SUCCESS'")
    Double getTotalPayoutForProvider(@Param("providerId") String providerId);

    @Query("SELECT p FROM Payout p WHERE p.status = 'FAILED' AND p.retryCount < 3")
    List<Payout> findRetryablePayouts();

    @Query("SELECT p FROM Payout p WHERE p.gatewayPayoutId = :gatewayPayoutId")
    Optional<Payout> findByGatewayPayoutId(@Param("gatewayPayoutId") String gatewayPayoutId);
}
