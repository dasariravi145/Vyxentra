package com.vyxentra.vehicle.repository;

import com.vyxentra.vehicle.entity.LocationUpdate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface LocationUpdateRepository extends JpaRepository<LocationUpdate, String> {

    Optional<LocationUpdate> findFirstByEntityIdAndEntityTypeOrderByCreatedAtDesc(
            String entityId, String entityType);

    List<LocationUpdate> findByEntityIdAndEntityTypeAndCreatedAtBetweenOrderByCreatedAtAsc(
            String entityId, String entityType, LocalDateTime from, LocalDateTime to);

    List<LocationUpdate> findByBookingIdOrderByCreatedAtAsc(String bookingId);

    @Query("SELECT l FROM LocationUpdate l WHERE l.bookingId = :bookingId " +
            "AND l.createdAt BETWEEN :start AND :end ORDER BY l.createdAt ASC")
    List<LocationUpdate> findPathForBooking(@Param("bookingId") String bookingId,
                                            @Param("start") LocalDateTime start,
                                            @Param("end") LocalDateTime end);

    @Modifying
    @Query("DELETE FROM LocationUpdate l WHERE l.createdAt < :cutoffDate")
    int deleteOlderThan(@Param("cutoffDate") LocalDateTime cutoffDate);

    @Query("SELECT COUNT(l) FROM LocationUpdate l WHERE l.entityId = :entityId " +
            "AND l.createdAt > :since")
    long countUpdatesSince(@Param("entityId") String entityId,
                           @Param("since") LocalDateTime since);
}
