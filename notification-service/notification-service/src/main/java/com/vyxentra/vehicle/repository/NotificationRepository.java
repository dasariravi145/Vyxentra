package com.vyxentra.vehicle.repository;


import com.vyxentra.vehicle.entity.Notification;
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
public interface NotificationRepository extends JpaRepository<Notification, String> {

    Optional<Notification> findByNotificationNumber(String notificationNumber);

    Page<Notification> findByUserIdOrderByCreatedAtDesc(String userId, Pageable pageable);

    @Query("SELECT n FROM Notification n WHERE n.userId = :userId " +
            "AND (:fromDate IS NULL OR n.createdAt >= :fromDate) " +
            "AND (:toDate IS NULL OR n.createdAt <= :toDate) " +
            "AND (:type IS NULL OR n.type = :type) " +
            "AND (:status IS NULL OR n.status = :status)")
    Page<Notification> findUserNotifications(@Param("userId") String userId,
                                             @Param("fromDate") LocalDateTime fromDate,
                                             @Param("toDate") LocalDateTime toDate,
                                             @Param("type") String type,
                                             @Param("status") String status,
                                             Pageable pageable);

    @Query("SELECT COUNT(n) FROM Notification n WHERE n.userId = :userId AND n.status = 'SENT'")
    Long countUnreadByUserId(@Param("userId") String userId);

    @Query("SELECT n FROM Notification n WHERE n.status = 'PENDING' AND n.retryCount < :maxRetries")
    List<Notification> findPendingNotifications(@Param("maxRetries") int maxRetries);

    @Modifying
    @Query("UPDATE Notification n SET n.status = 'READ', n.readAt = :now WHERE n.id = :notificationId")
    void markAsRead(@Param("notificationId") String notificationId, @Param("now") LocalDateTime now);

    @Modifying
    @Query("UPDATE Notification n SET n.status = 'READ', n.readAt = :now WHERE n.userId = :userId AND n.status = 'SENT'")
    int markAllAsRead(@Param("userId") String userId, @Param("now") LocalDateTime now);
}
