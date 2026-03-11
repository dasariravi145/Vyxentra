package com.vyxentra.vehicle.repository;


import com.vyxentra.vehicle.dto.DamageItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface DamageItemRepository extends JpaRepository<DamageItem, String> {

    List<DamageItem> findByDamageReportId(String damageReportId);

    @Modifying
    @Query("UPDATE DamageItem di SET di.isApproved = :approved, di.approvedCost = :cost, " +
            "di.approvedAt = :approvedAt WHERE di.id = :itemId")
    void updateApproval(@Param("itemId") String itemId,
                        @Param("approved") Boolean approved,
                        @Param("cost") Double cost,
                        @Param("approvedAt") LocalDateTime approvedAt);

    @Modifying
    @Query("UPDATE DamageItem di SET di.rejectionReason = :reason WHERE di.id = :itemId")
    void updateRejectionReason(@Param("itemId") String itemId,
                               @Param("reason") String reason);
}
