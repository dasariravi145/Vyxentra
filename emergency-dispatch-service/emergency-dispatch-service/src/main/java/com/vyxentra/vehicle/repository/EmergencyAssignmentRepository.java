package com.vyxentra.vehicle.repository;


import com.vyxentra.vehicle.entity.EmergencyAssignment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EmergencyAssignmentRepository extends JpaRepository<EmergencyAssignment, String> {

    Optional<EmergencyAssignment> findByRequestId(String requestId);

    Optional<EmergencyAssignment> findByBookingId(String bookingId);

    @Query("SELECT a FROM EmergencyAssignment a WHERE a.providerId = :providerId " +
            "AND a.status IN ('ACCEPTED', 'ARRIVED')")
    Optional<EmergencyAssignment> findActiveByProviderId(@Param("providerId") String providerId);

    List<EmergencyAssignment> findByProviderIdOrderByCreatedAtDesc(String providerId);

    @Query("SELECT COUNT(a) > 0 FROM EmergencyAssignment a WHERE a.request.id = :requestId")
    boolean existsByRequestId(@Param("requestId") String requestId);
}
