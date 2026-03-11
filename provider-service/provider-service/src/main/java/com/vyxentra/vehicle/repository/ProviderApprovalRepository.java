package com.vyxentra.vehicle.repository;

import com.vyxentra.vehicle.entity.ProviderApproval;
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
public interface ProviderApprovalRepository extends JpaRepository<ProviderApproval, String> {

    Optional<ProviderApproval> findByProviderId(String providerId);

    @Query("SELECT pa FROM ProviderApproval pa WHERE pa.assignedAdmin IS NULL")
    Page<ProviderApproval> findUnassigned(Pageable pageable);

    @Query("SELECT pa FROM ProviderApproval pa WHERE pa.provider.status = 'PENDING_APPROVAL'")
    Page<ProviderApproval> findPendingApprovals(Pageable pageable);

    @Query("SELECT pa FROM ProviderApproval pa WHERE pa.assignedAdmin = :adminId AND pa.reviewedAt IS NULL")
    List<ProviderApproval> findAssignedToAdmin(@Param("adminId") String adminId);

    @Query("SELECT COUNT(pa) FROM ProviderApproval pa WHERE pa.assignedAdmin IS NULL")
    long countUnassigned();

    @Modifying
    @Query("UPDATE ProviderApproval pa SET pa.assignedAdmin = :adminId, pa.assignedAt = :now " +
            "WHERE pa.provider.id = :providerId")
    void assignApproval(@Param("providerId") String providerId,
                        @Param("adminId") String adminId,
                        @Param("now") Instant now);
}
