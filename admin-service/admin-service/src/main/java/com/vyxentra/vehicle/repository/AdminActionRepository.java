package com.vyxentra.vehicle.repository;


import com.vyxentra.vehicle.entity.AdminAction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AdminActionRepository extends JpaRepository<AdminAction, String> {

    Page<AdminAction> findByAdminIdOrderByCreatedAtDesc(String adminId, Pageable pageable);

    List<AdminAction> findByTargetTypeAndTargetIdOrderByCreatedAtDesc(String targetType, String targetId);

    @Query("SELECT a FROM AdminAction a WHERE a.adminId = :adminId " +
            "AND a.createdAt BETWEEN :fromDate AND :toDate")
    List<AdminAction> findByAdminAndDateRange(@Param("adminId") String adminId,
                                              @Param("fromDate") LocalDateTime fromDate,
                                              @Param("toDate") LocalDateTime toDate);

    @Query("SELECT a.actionType, COUNT(a) FROM AdminAction a " +
            "WHERE a.createdAt >= :since GROUP BY a.actionType")
    List<Object[]> countActionsByType(@Param("since") LocalDateTime since);
}
