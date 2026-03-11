package com.vyxentra.vehicle.repository;


import com.vyxentra.vehicle.entity.ServiceCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ServiceCategoryRepository extends JpaRepository<ServiceCategory, String> {

    Optional<ServiceCategory> findByName(String name);

    List<ServiceCategory> findByIsActiveTrueOrderByDisplayOrderAsc();

    @Query("SELECT c, COUNT(s) FROM ServiceCategory c LEFT JOIN c.services s " +
            "WHERE (:active IS NULL OR c.isActive = :active) " +
            "GROUP BY c ORDER BY c.displayOrder ASC")
    List<Object[]> findCategoriesWithServiceCount(@Param("active") Boolean active);

    boolean existsByName(String name);
}
