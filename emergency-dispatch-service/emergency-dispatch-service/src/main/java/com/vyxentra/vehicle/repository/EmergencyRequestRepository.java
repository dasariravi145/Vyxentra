package com.vyxentra.vehicle.repository;


import com.vyxentra.vehicle.entity.EmergencyRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface EmergencyRequestRepository extends JpaRepository<EmergencyRequest, String> {

    Optional<EmergencyRequest> findByRequestNumber(String requestNumber);

    List<EmergencyRequest> findByCustomerIdOrderByCreatedAtDesc(String customerId);

    @Query("SELECT e FROM EmergencyRequest e WHERE e.customerId = :customerId " +
            "AND e.status IN ('SEARCHING', 'ASSIGNED')")
    Optional<EmergencyRequest> findActiveByCustomerId(@Param("customerId") String customerId);

    @Query("SELECT e FROM EmergencyRequest e WHERE e.status = 'SEARCHING' AND e.expiryTime < :now")
    List<EmergencyRequest> findExpiredRequests(@Param("now") LocalDateTime now);

    @Query("SELECT e FROM EmergencyRequest e WHERE e.status = 'SEARCHING' " +
            "AND e.currentRadiusKm < e.maxRadiusKm")
    List<EmergencyRequest> findRequestsForRadiusExpansion();

    @Modifying
    @Query("UPDATE EmergencyRequest e SET e.status = 'EXPIRED' WHERE e.id = :requestId")
    void markAsExpired(@Param("requestId") String requestId);

    @Modifying
    @Query("UPDATE EmergencyRequest e SET e.currentRadiusKm = e.currentRadiusKm + :increment, " +
            "e.expiryTime = :newExpiry WHERE e.id = :requestId")
    void expandRadius(@Param("requestId") String requestId,
                      @Param("increment") int increment,
                      @Param("newExpiry") LocalDateTime newExpiry);
}
