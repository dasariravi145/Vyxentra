package com.vyxentra.vehicle.repository;

import com.vyxentra.vehicle.entity.PaymentMethod;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentMethodRepository extends JpaRepository<PaymentMethod, String> {

    List<PaymentMethod> findByUserIdAndIsActiveTrue(String userId);

    Optional<PaymentMethod> findByUserIdAndIsDefaultTrue(String userId);

    @Query("SELECT pm FROM PaymentMethod pm WHERE pm.userId = :userId AND pm.isActive = true ORDER BY pm.isDefault DESC, pm.createdAt DESC")
    List<PaymentMethod> findUserPaymentMethods(@Param("userId") String userId);

    @Modifying
    @Query("UPDATE PaymentMethod pm SET pm.isDefault = false WHERE pm.userId = :userId")
    void resetDefaultForUser(@Param("userId") String userId);

    boolean existsByGatewayToken(String gatewayToken);
    long countByUserIdAndIsActiveTrue(String userId);
}
