package com.vyxentra.vehicle.repository;
import com.vyxentra.vehicle.entity.ProviderNotification;
import com.vyxentra.vehicle.enums.ProviderResponseStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ProviderNotificationRepository extends JpaRepository<ProviderNotification, String> {

    List<ProviderNotification> findByRequestId(String requestId);

    Optional<ProviderNotification> findByRequestIdAndProviderId(String requestId, String providerId);

    @Query("SELECT pn FROM ProviderNotification pn WHERE pn.request.id = :requestId AND pn.responseStatus = 'PENDING'")
    List<ProviderNotification> findPendingByRequestId(@Param("requestId") String requestId);

    @Query("SELECT pn FROM ProviderNotification pn WHERE pn.responseStatus = 'PENDING' AND pn.notifiedAt < :timeout")
    List<ProviderNotification> findExpiredNotifications(@Param("timeout") LocalDateTime timeout);

    @Modifying
    @Query("UPDATE ProviderNotification pn SET pn.responseStatus = :status, pn.respondedAt = :now " +
            "WHERE pn.id = :notificationId")
    void updateResponseStatus(@Param("notificationId") String notificationId,
                              @Param("status") ProviderResponseStatus status,
                              @Param("now") LocalDateTime now);

    boolean existsByRequestIdAndProviderId(String requestId, String providerId);

    long countByRequestIdAndResponseStatus(String requestId, ProviderResponseStatus status);
}
