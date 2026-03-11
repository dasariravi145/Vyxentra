package com.vyxentra.vehicle.repository;


import com.vyxentra.vehicle.entity.TransactionLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Repository
public interface TransactionLogRepository extends JpaRepository<TransactionLog, String> {

    List<TransactionLog> findByTransactionId(String transactionId);

    Optional<TransactionLog> findByTransactionIdAndAction(String transactionId, String action);

    List<TransactionLog> findByReferenceId(String referenceId);

    Page<TransactionLog> findByUserId(String userId, Pageable pageable);

    @Query("SELECT t FROM TransactionLog t WHERE t.userId = :userId AND t.createdAt BETWEEN :start AND :end")
    Page<TransactionLog> findByUserIdAndDateRange(@Param("userId") String userId,
                                                  @Param("start") Instant start,
                                                  @Param("end") Instant end,
                                                  Pageable pageable);

    Page<TransactionLog> findByTransactionType(String transactionType, Pageable pageable);

    Page<TransactionLog> findByStatus(String status, Pageable pageable);

    Page<TransactionLog> findBySuccess(Boolean success, Pageable pageable);

    @Query("SELECT t FROM TransactionLog t WHERE t.success = false AND t.errorMessage IS NOT NULL")
    Page<TransactionLog> findFailedTransactions(Pageable pageable);

    Optional<TransactionLog> findByGatewayTransactionId(String gatewayTransactionId);

    @Query("SELECT t.transactionType, COUNT(t) FROM TransactionLog t GROUP BY t.transactionType")
    List<Object[]> getTransactionTypeDistribution();

    @Query("SELECT t.status, COUNT(t) FROM TransactionLog t GROUP BY t.status")
    List<Object[]> getStatusDistribution();

    @Query("SELECT DATE(t.createdAt), COUNT(t), SUM(t.amount) FROM TransactionLog t " +
            "WHERE t.createdAt BETWEEN :start AND :end AND t.success = true " +
            "GROUP BY DATE(t.createdAt)")
    List<Object[]> getDailySummary(@Param("start") Instant start, @Param("end") Instant end);

    @Modifying
    @Query("DELETE FROM TransactionLog t WHERE t.createdAt < :cutoff")
    int deleteOlderThan(@Param("cutoff") Instant cutoff);

    List<TransactionLog> findByIpAddress(String ipAddress);

    List<TransactionLog> findByDeviceId(String deviceId);

    @Query("SELECT COUNT(t) FROM TransactionLog t WHERE t.userId = :userId " +
            "AND t.success = false AND t.createdAt > :since")
    long countFailedAttempts(@Param("userId") String userId, @Param("since") Instant since);
}
