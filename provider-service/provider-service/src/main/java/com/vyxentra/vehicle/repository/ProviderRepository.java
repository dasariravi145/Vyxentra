package com.vyxentra.vehicle.repository;


import com.vyxentra.vehicle.entity.Provider;
import com.vyxentra.vehicle.enums.ProviderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Repository
public interface ProviderRepository extends JpaRepository<Provider, String>, JpaSpecificationExecutor<Provider> {

    Optional<Provider> findByUserId(String userId);

    Optional<Provider> findByGstNumber(String gstNumber);

    Optional<Provider> findByEmail(String email);

    Optional<Provider> findByPhone(String phone);

    Page<Provider> findByStatus(ProviderStatus status, Pageable pageable);

    List<Provider> findByStatus(ProviderStatus status);

    List<Provider> findByStatusIn(List<ProviderStatus> statuses);

    @Query("SELECT p FROM Provider p WHERE p.status = :status AND p.city = :city")
    List<Provider> findByStatusAndCity(@Param("status") ProviderStatus status, @Param("city") String city);

    @Query(value = "SELECT p.*, " +
            "(6371 * acos(cos(radians(:lat)) * cos(radians(p.latitude)) * " +
            "cos(radians(p.longitude) - radians(:lng)) + sin(radians(:lat)) * sin(radians(p.latitude)))) AS distance " +
            "FROM providers p " +
            "WHERE p.status = 'ACTIVE' " +
            "AND (:supportsBike IS NULL OR p.supports_bike = :supportsBike) " +
            "AND (:supportsCar IS NULL OR p.supports_car = :supportsCar) " +
            "AND (:providerType IS NULL OR p.provider_type = :providerType) " +
            "HAVING distance < :radiusKm " +
            "ORDER BY distance " +
            "LIMIT :limit", nativeQuery = true)
    List<Provider> findNearbyActiveProviders(@Param("lat") double latitude,
                                             @Param("lng") double longitude,
                                             @Param("radiusKm") double radiusKm,
                                             @Param("supportsBike") Boolean supportsBike,
                                             @Param("supportsCar") Boolean supportsCar,
                                             @Param("providerType") String providerType,
                                             @Param("limit") int limit);

    default List<Provider> findNearbyActiveProviders(double latitude, double longitude,
                                                     double radiusKm) {
        return findNearbyActiveProviders(latitude, longitude, radiusKm, null, null, null, 20);
    }

    @Query("SELECT COUNT(p) FROM Provider p WHERE p.status = :status")
    long countByStatus(@Param("status") ProviderStatus status);

    boolean existsByGstNumber(String gstNumber);

    boolean existsByEmail(String email);

    boolean existsByPhone(String phone);

    @Query("SELECT COUNT(p) > 0 FROM Provider p WHERE p.id = :providerId AND p.status = 'ACTIVE'")
    boolean isProviderActive(@Param("providerId") String providerId);

    @Modifying
    @Query("UPDATE Provider p SET p.totalBookings = COALESCE(p.totalBookings, 0) + 1 WHERE p.id = :providerId")
    void incrementBookingCount(@Param("providerId") String providerId);

    @Modifying
    @Query("UPDATE Provider p SET p.averageRating = :rating, p.totalReviews = :totalReviews WHERE p.id = :providerId")
    void updateRating(@Param("providerId") String providerId,
                      @Param("rating") Double rating,
                      @Param("totalReviews") Integer totalReviews);
}
