package com.vyxentra.vehicle.repository;


import com.vyxentra.vehicle.entity.ProviderPricing;
import com.vyxentra.vehicle.enums.ServiceType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface ProviderPricingRepository extends JpaRepository<ProviderPricing, String> {

    List<ProviderPricing> findByProviderId(String providerId);

    List<ProviderPricing> findByProviderIdAndIsActiveTrue(String providerId);

    Optional<ProviderPricing> findByProviderIdAndServiceType(String providerId, ServiceType serviceType);

    @Query("SELECT pp FROM ProviderPricing pp WHERE pp.provider.id = :providerId " +
            "AND pp.serviceType IN :serviceTypes")
    List<ProviderPricing> findByProviderIdAndServiceTypes(@Param("providerId") String providerId,
                                                          @Param("serviceTypes") List<ServiceType> serviceTypes);

    boolean existsByProviderIdAndServiceType(String providerId, ServiceType serviceType);

    @Modifying
    @Query("UPDATE ProviderPricing pp SET pp.isActive = :active WHERE pp.id = :pricingId")
    void setActiveStatus(@Param("pricingId") String pricingId, @Param("active") Boolean active);

    @Modifying
    @Query("UPDATE ProviderPricing pp SET pp.basePrice = :price WHERE pp.id = :pricingId")
    void updatePrice(@Param("pricingId") String pricingId, @Param("price") BigDecimal price);
}
