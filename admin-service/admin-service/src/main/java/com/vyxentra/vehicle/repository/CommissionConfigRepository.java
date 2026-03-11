package com.vyxentra.vehicle.repository;


import com.vyxentra.vehicle.entity.CommissionConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface CommissionConfigRepository extends JpaRepository<CommissionConfig, String> {

    Optional<CommissionConfig> findByProviderTypeAndIsActiveTrue(String providerType);

    List<CommissionConfig> findByIsActiveTrue();

    @Query("SELECT c FROM CommissionConfig c WHERE c.providerType = :providerType " +
            "AND c.effectiveFrom <= :date AND (c.effectiveTo IS NULL OR c.effectiveTo >= :date)")
    Optional<CommissionConfig> findEffectiveConfig(@Param("providerType") String providerType,
                                                   @Param("date") LocalDate date);
}
