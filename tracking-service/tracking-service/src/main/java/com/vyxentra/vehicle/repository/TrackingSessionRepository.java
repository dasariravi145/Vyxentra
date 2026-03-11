package com.vyxentra.vehicle.repository;

import com.vyxentra.vehicle.entity.TrackingSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface TrackingSessionRepository extends JpaRepository<TrackingSession, String> {

    Optional<TrackingSession> findByBookingId(String bookingId);

    List<TrackingSession> findByCustomerIdOrderByCreatedAtDesc(String customerId);

    List<TrackingSession> findByProviderIdOrderByCreatedAtDesc(String providerId);

    List<TrackingSession> findByEmployeeIdOrderByCreatedAtDesc(String employeeId);

    @Query("SELECT t FROM TrackingSession t WHERE t.customerId = :userId " +
            "AND t.createdAt BETWEEN :from AND :to ORDER BY t.createdAt DESC")
    List<TrackingSession> findByUserAndDateRange(@Param("userId") String userId,
                                                 @Param("from") LocalDateTime from,
                                                 @Param("to") LocalDateTime to);

    @Query("SELECT t FROM TrackingSession t WHERE t.status = 'ACTIVE' " +
            "AND t.lastUpdateAt < :timeout")
    List<TrackingSession> findStaleSessions(@Param("timeout") LocalDateTime timeout);

    @Query("SELECT t FROM TrackingSession t WHERE t.status = 'ACTIVE' " +
            "AND t.expiresAt < :now")
    List<TrackingSession> findExpiredSessions(@Param("now") LocalDateTime now);

    @Modifying
    @Query("UPDATE TrackingSession t SET t.status = 'EXPIRED' WHERE t.id = :sessionId")
    void markAsExpired(@Param("sessionId") String sessionId);

    @Modifying
    @Query("UPDATE TrackingSession t SET t.currentLocationLat = :lat, " +
            "t.currentLocationLng = :lng, t.lastUpdateAt = :lastUpdate, " +
            "t.currentEtaMinutes = :eta WHERE t.id = :sessionId")
    void updateLocationAndETA(@Param("sessionId") String sessionId,
                              @Param("lat") Double lat,
                              @Param("lng") Double lng,
                              @Param("lastUpdate") LocalDateTime lastUpdate,
                              @Param("eta") Integer eta);
}
