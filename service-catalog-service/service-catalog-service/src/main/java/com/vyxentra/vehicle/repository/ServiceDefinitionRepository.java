package com.vyxentra.vehicle.repository;

import com.vyxentra.vehicle.entity.ServiceDefinition;
import com.vyxentra.vehicle.enums.ProviderType;
import com.vyxentra.vehicle.enums.ServiceType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ServiceDefinitionRepository extends JpaRepository<ServiceDefinition, String>,
        JpaSpecificationExecutor<ServiceDefinition> {

    Optional<ServiceDefinition> findByServiceType(ServiceType serviceType);

    List<ServiceDefinition> findByIsActiveTrue();

    List<ServiceDefinition> findByCategoryIdAndIsActiveTrue(String categoryId);

    @Query("SELECT s FROM ServiceDefinition s WHERE s.isActive = true AND s.isPopular = true")
    List<ServiceDefinition> findPopularServices();

    @Query("SELECT s FROM ServiceDefinition s WHERE s.isActive = true AND s.isRecommended = true")
    List<ServiceDefinition> findRecommendedServices();

    @Query("SELECT DISTINCT s FROM ServiceDefinition s JOIN s.vehicleTypes vt " +
            "WHERE s.isActive = true AND vt.vehicleType = :vehicleType AND vt.isActive = true")
    List<ServiceDefinition> findByVehicleType(@Param("vehicleType") String vehicleType);

    @Query("SELECT s FROM ServiceDefinition s WHERE s.providerType = :providerType AND s.isActive = true")
    List<ServiceDefinition> findByProviderType(@Param("providerType") ProviderType providerType);

    @Query("SELECT s FROM ServiceDefinition s WHERE " +
            "(:categoryId IS NULL OR s.category.id = :categoryId) AND " +
            "(:vehicleType IS NULL OR EXISTS (SELECT vt FROM s.vehicleTypes vt WHERE vt.vehicleType = :vehicleType)) AND " +
            "(:providerType IS NULL OR s.providerType = :providerType) AND " +
            "(:isActive IS NULL OR s.isActive = :isActive)")
    Page<ServiceDefinition> findByFilters(@Param("categoryId") String categoryId,
                                          @Param("vehicleType") String vehicleType,
                                          @Param("providerType") ProviderType providerType,
                                          @Param("isActive") Boolean isActive,
                                          Pageable pageable);

    @Query("SELECT s FROM ServiceDefinition s WHERE " +
            "LOWER(s.name) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
            "LOWER(s.description) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
            "LOWER(s.shortDescription) LIKE LOWER(CONCAT('%', :query, '%'))")
    List<ServiceDefinition> searchByQuery(@Param("query") String query);

    boolean existsByServiceType(ServiceType serviceType);
}
