package com.vyxentra.vehicle.repository;

import com.vyxentra.vehicle.dto.PathPoint;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PathPointRepository extends JpaRepository<PathPoint, String> {

    List<PathPoint> findByTrackingSessionIdOrderBySequenceAsc(String trackingSessionId);

    @Query("SELECT p FROM PathPoint p WHERE p.trackingSession.id = :sessionId " +
            "ORDER BY p.timestamp ASC")
    List<PathPoint> findPathBySessionId(@Param("sessionId") String sessionId);

    @Query("SELECT COUNT(p) FROM PathPoint p WHERE p.trackingSession.id = :sessionId")
    int countBySessionId(@Param("sessionId") String sessionId);
}