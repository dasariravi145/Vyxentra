package com.vyxentra.vehicle.repository;


import com.vyxentra.vehicle.entity.DashboardCache;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository for managing dashboard cache entries
 */
@Repository
public interface DashboardCacheRepository extends JpaRepository<DashboardCache, String> {

    /**
     * Find cache entry by cache key
     */
    Optional<DashboardCache> findByCacheKey(String cacheKey);

    /**
     * Find cache entry by cache key that is not expired
     */
    @Query("SELECT d FROM DashboardCache d WHERE d.cacheKey = :cacheKey AND d.expiresAt > :now")
    Optional<DashboardCache> findValidByCacheKey(@Param("cacheKey") String cacheKey,
                                                 @Param("now") LocalDateTime now);

    /**
     * Find all cache entries that are still valid
     */
    @Query("SELECT d FROM DashboardCache d WHERE d.expiresAt > :now")
    List<DashboardCache> findAllValid(@Param("now") LocalDateTime now);

    /**
     * Find all cache entries that have expired
     */
    @Query("SELECT d FROM DashboardCache d WHERE d.expiresAt <= :now")
    List<DashboardCache> findAllExpired(@Param("now") LocalDateTime now);

    /**
     * Find cache entries by cache key pattern
     */
    @Query("SELECT d FROM DashboardCache d WHERE d.cacheKey LIKE :pattern")
    List<DashboardCache> findByCacheKeyPattern(@Param("pattern") String pattern);

    /**
     * Find cache entries by user ID
     */
    @Query("SELECT d FROM DashboardCache d WHERE d.createdBy = :userId")
    List<DashboardCache> findByCreatedBy(@Param("userId") String userId);

    /**
     * Delete cache entry by cache key
     */
    @Modifying
    @Query("DELETE FROM DashboardCache d WHERE d.cacheKey = :cacheKey")
    int deleteByCacheKey(@Param("cacheKey") String cacheKey);

    /**
     * Delete all cache entries with keys matching pattern
     */
    @Modifying
    @Query("DELETE FROM DashboardCache d WHERE d.cacheKey LIKE :pattern")
    int deleteByCacheKeyPattern(@Param("pattern") String pattern);

    /**
     * Delete all expired cache entries
     */
    @Modifying
    @Query("DELETE FROM DashboardCache d WHERE d.expiresAt <= :now")
    int deleteAllExpired(@Param("now") LocalDateTime now);

    /**
     * Delete all cache entries older than specified date
     */
    @Modifying
    @Query("DELETE FROM DashboardCache d WHERE d.createdAt < :cutoffDate")
    int deleteOlderThan(@Param("cutoffDate") LocalDateTime cutoffDate);

    /**
     * Update expiry time for a cache entry
     */
    @Modifying
    @Query("UPDATE DashboardCache d SET d.expiresAt = :expiresAt WHERE d.cacheKey = :cacheKey")
    int updateExpiry(@Param("cacheKey") String cacheKey, @Param("expiresAt") LocalDateTime expiresAt);

    /**
     * Count valid cache entries
     */
    @Query("SELECT COUNT(d) FROM DashboardCache d WHERE d.expiresAt > :now")
    long countValid(@Param("now") LocalDateTime now);

    /**
     * Count expired cache entries
     */
    @Query("SELECT COUNT(d) FROM DashboardCache d WHERE d.expiresAt <= :now")
    long countExpired(@Param("now") LocalDateTime now);

    /**
     * Check if cache key exists and is valid
     */
    @Query("SELECT CASE WHEN COUNT(d) > 0 THEN true ELSE false END FROM DashboardCache d " +
            "WHERE d.cacheKey = :cacheKey AND d.expiresAt > :now")
    boolean existsAndValid(@Param("cacheKey") String cacheKey, @Param("now") LocalDateTime now);

    /**
     * Get cache statistics
     */
    @Query("SELECT COUNT(d), AVG(EXTRACT(EPOCH FROM (d.expiresAt - d.createdAt))) FROM DashboardCache d")
    Object[] getCacheStatistics();

    /**
     * Get cache size by type (based on key pattern)
     */
    @Query("SELECT SUBSTRING(d.cacheKey, 1, POSITION(':' IN d.cacheKey) - 1) as type, COUNT(d) " +
            "FROM DashboardCache d GROUP BY type")
    List<Object[]> getCacheSizeByType();

    /**
     * Find caches that will expire soon
     */
    @Query("SELECT d FROM DashboardCache d WHERE d.expiresAt BETWEEN :now AND :threshold")
    List<DashboardCache> findExpiringSoon(@Param("now") LocalDateTime now,
                                          @Param("threshold") LocalDateTime threshold);

    /**
     * Bulk update expiry for multiple cache keys
     */
    @Modifying
    @Query("UPDATE DashboardCache d SET d.expiresAt = :expiresAt WHERE d.cacheKey IN :cacheKeys")
    int bulkUpdateExpiry(@Param("cacheKeys") List<String> cacheKeys,
                         @Param("expiresAt") LocalDateTime expiresAt);

    /**
     * Get oldest cache entries for cleanup
     */
    @Query("SELECT d FROM DashboardCache d ORDER BY d.createdAt ASC LIMIT :limit")
    List<DashboardCache> findOldest(@Param("limit") int limit);

    @Modifying
    @Transactional
    @Query("DELETE FROM DashboardCache d WHERE d.cacheKey LIKE :prefix%")
    int deleteByCacheKeyStartsWith(@Param("prefix") String prefix);
}
