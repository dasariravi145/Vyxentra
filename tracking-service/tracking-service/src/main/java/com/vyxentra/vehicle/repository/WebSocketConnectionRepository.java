package com.vyxentra.vehicle.repository;

import com.vyxentra.vehicle.entity.WebSocketConnection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface WebSocketConnectionRepository extends JpaRepository<WebSocketConnection, String> {

    Optional<WebSocketConnection> findBySessionId(String sessionId);

    List<WebSocketConnection> findByUserId(String userId);

    List<WebSocketConnection> findByUserIdAndDisconnectedAtIsNull(String userId);

    List<WebSocketConnection> findByTrackingSessionId(String trackingSessionId);

    @Query("SELECT w FROM WebSocketConnection w WHERE w.lastHeartbeatAt < :timeout AND w.disconnectedAt IS NULL")
    List<WebSocketConnection> findStaleConnections(@Param("timeout") LocalDateTime timeout);

    @Query("SELECT COUNT(w) FROM WebSocketConnection w WHERE w.trackingSessionId = :trackingSessionId AND w.disconnectedAt IS NULL")
    long countActiveByTrackingSessionId(@Param("trackingSessionId") String trackingSessionId);

    @Modifying
    @Query("UPDATE WebSocketConnection w SET w.disconnectedAt = :now WHERE w.sessionId = :sessionId")
    void markAsDisconnected(@Param("sessionId") String sessionId, @Param("now") LocalDateTime now);

    @Modifying
    @Query("UPDATE WebSocketConnection w SET w.lastHeartbeatAt = :heartbeat WHERE w.sessionId = :sessionId")
    void updateHeartbeat(@Param("sessionId") String sessionId, @Param("heartbeat") LocalDateTime heartbeat);

    @Modifying
    @Query("DELETE FROM WebSocketConnection w WHERE w.disconnectedAt < :cutoff")
    int deleteOldDisconnected(@Param("cutoff") LocalDateTime cutoff);
}
