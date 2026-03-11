package com.vyxentra.vehicle.repository;


import com.vyxentra.vehicle.entity.AuditLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, String> {

    @Query("SELECT a FROM AuditLog a WHERE " +
            "(:fromDate IS NULL OR a.createdAt >= :fromDate) " +
            "AND (:toDate IS NULL OR a.createdAt <= :toDate) " +
            "AND (:eventType IS NULL OR a.eventType = :eventType) " +
            "AND (:userId IS NULL OR a.userId = :userId) " +
            "AND (:resourceType IS NULL OR a.resourceType = :resourceType) " +
            "AND (:success IS NULL OR a.success = :success)")
    Page<AuditLog> findAuditLogs(@Param("fromDate") LocalDateTime fromDate,
                                 @Param("toDate") LocalDateTime toDate,
                                 @Param("eventType") String eventType,
                                 @Param("userId") String userId,
                                 @Param("resourceType") String resourceType,
                                 @Param("success") Boolean success,
                                 Pageable pageable);

    @Query("SELECT COUNT(a), a.eventType FROM AuditLog a " +
            "WHERE a.createdAt >= :since GROUP BY a.eventType")
    List<Object[]> getEventTypeDistribution(@Param("since") LocalDateTime since);

    @Modifying
    @Query("DELETE FROM AuditLog a WHERE a.createdAt < :cutoffDate")
    int deleteOlderThan(@Param("cutoffDate") LocalDateTime cutoffDate);

    @Query("SELECT AVG(a.durationMs) FROM AuditLog a " +
            "WHERE a.createdAt >= :since AND a.serviceName = :serviceName")
    Double getAverageResponseTime(@Param("serviceName") String serviceName,
                                  @Param("since") LocalDateTime since);
}