package com.vyxentra.vehicle.repository;


import com.vyxentra.vehicle.entity.ServiceItem;
import com.vyxentra.vehicle.enums.ServiceStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ServiceItemRepository extends JpaRepository<ServiceItem, Long> {
    Page<ServiceItem> findByCategoryId(Long categoryId, Pageable pageable);
    Page<ServiceItem> findByStatus(ServiceStatus status, Pageable pageable);
    Optional<ServiceItem> findBySlug(String slug);
    List<ServiceItem> findByCategoryIdAndStatus(Long categoryId, ServiceStatus status);

    @Query("""
    SELECT DISTINCT s FROM ServiceItem s
    JOIN s.tags t
    WHERE LOWER(s.name) LIKE LOWER(CONCAT('%', :keyword, '%'))
       OR LOWER(s.description) LIKE LOWER(CONCAT('%', :keyword, '%'))
       OR LOWER(t) LIKE LOWER(CONCAT('%', :keyword, '%'))
""")
    Page<ServiceItem> searchByKeyword(@Param("keyword") String keyword, Pageable pageable);

    List<ServiceItem> findByIsFeaturedTrueAndStatusOrderByPopularityScoreDesc(ServiceStatus status);
}