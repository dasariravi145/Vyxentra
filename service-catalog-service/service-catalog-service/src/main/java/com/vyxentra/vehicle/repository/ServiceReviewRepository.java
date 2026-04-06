package com.vyxentra.vehicle.repository;


import com.vyxentra.vehicle.entity.ServiceReview;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ServiceReviewRepository extends JpaRepository<ServiceReview, Long> {
    Page<ServiceReview> findByServiceId(Long serviceId, Pageable pageable);
    List<ServiceReview> findByServiceIdAndRatingGreaterThanEqual(Long serviceId, Integer minRating);

    @Query("SELECT AVG(r.rating) FROM ServiceReview r WHERE r.serviceId = :serviceId")
    Double getAverageRatingByServiceId(@Param("serviceId") Long serviceId);

    @Query("SELECT COUNT(r) FROM ServiceReview r WHERE r.serviceId = :serviceId")
    Long getReviewCountByServiceId(@Param("serviceId") Long serviceId);
}
