package com.vyxentra.vehicle.repository;


import com.vyxentra.vehicle.entity.ServiceCategory;
import com.vyxentra.vehicle.enums.CategoryType;
import com.vyxentra.vehicle.enums.ServiceStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ServiceCategoryRepository extends JpaRepository<ServiceCategory, Long> {
    Optional<ServiceCategory> findByType(CategoryType type);
    Optional<ServiceCategory> findBySlug(String slug);
    List<ServiceCategory> findByStatusOrderByDisplayOrderAsc(ServiceStatus status);
    List<ServiceCategory> findByIsPopularTrueAndStatus(ServiceStatus status);
}