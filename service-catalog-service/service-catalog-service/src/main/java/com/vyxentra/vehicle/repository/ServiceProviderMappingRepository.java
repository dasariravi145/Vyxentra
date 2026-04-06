package com.vyxentra.vehicle.repository;


import com.vyxentra.vehicle.entity.ServiceProviderMapping;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ServiceProviderMappingRepository extends JpaRepository<ServiceProviderMapping, Long> {
    Optional<ServiceProviderMapping> findByProviderIdAndServiceId(Long providerId, Long serviceId);
    List<ServiceProviderMapping> findByProviderId(Long providerId);
    List<ServiceProviderMapping> findByServiceId(Long serviceId);
    List<ServiceProviderMapping> findByServiceIdAndIsAvailableTrue(Long serviceId);

    @Query("SELECT sp.serviceId FROM ServiceProviderMapping sp WHERE sp.providerId = :providerId AND sp.isAvailable = true")
    List<Long> findAvailableServiceIdsByProviderId(@Param("providerId") Long providerId);
}