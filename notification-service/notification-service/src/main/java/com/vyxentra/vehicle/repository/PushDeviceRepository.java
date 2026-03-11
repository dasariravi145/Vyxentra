package com.vyxentra.vehicle.repository;


import com.vyxentra.vehicle.entity.PushDevice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository for managing push notification devices
 */
@Repository
public interface PushDeviceRepository extends JpaRepository<PushDevice, String> {

    // ==================== Basic Find Methods ====================

    /**
     * Find device by device token
     */
    Optional<PushDevice> findByDeviceToken(String deviceToken);

    /**
     * Find all active devices for a user
     */
    List<PushDevice> findByUserIdAndIsActiveTrue(String userId);

    /**
     * Find all devices for a user (including inactive)
     */
    List<PushDevice> findByUserId(String userId);

    /**
     * Find device by ID and user ID
     */
    Optional<PushDevice> findByIdAndUserId(String id, String userId);

    // ==================== Find by User Type ====================

    /**
     * Find all active devices for users of a specific type
     */
    @Query("SELECT d FROM PushDevice d WHERE d.userId IN " +
            "(SELECT u.id FROM User u WHERE u.userType = :userType) AND d.isActive = true")
    List<PushDevice> findByUserType(@Param("userType") String userType);

    /**
     * Find all devices for multiple user IDs
     */
    @Query("SELECT d FROM PushDevice d WHERE d.userId IN :userIds AND d.isActive = true")
    List<PushDevice> findByUserIds(@Param("userIds") List<String> userIds);

    // ==================== Find by Platform ====================

    /**
     * Find all active devices for a specific platform
     */
    List<PushDevice> findByPlatformAndIsActiveTrue(String platform);

    /**
     * Find all devices by platform
     */
    List<PushDevice> findByPlatform(String platform);

    // ==================== Find by App Version ====================

    /**
     * Find devices by app version
     */
    List<PushDevice> findByAppVersion(String appVersion);

    /**
     * Find devices with outdated app versions
     */
    @Query("SELECT d FROM PushDevice d WHERE d.appVersion < :minVersion AND d.isActive = true")
    List<PushDevice> findByAppVersionLessThan(@Param("minVersion") String minVersion);

    // ==================== Find by Last Used ====================

    /**
     * Find devices that haven't been used since a cutoff date
     */
    @Query("SELECT d FROM PushDevice d WHERE d.lastUsedAt < :cutoff AND d.isActive = true")
    List<PushDevice> findInactiveDevices(@Param("cutoff") LocalDateTime cutoff);

    /**
     * Find devices used within a date range
     */
    @Query("SELECT d FROM PushDevice d WHERE d.lastUsedAt BETWEEN :start AND :end")
    List<PushDevice> findDevicesUsedBetween(@Param("start") LocalDateTime start,
                                            @Param("end") LocalDateTime end);

    // ==================== Find Recently Active ====================

    /**
     * Find recently active devices (last N minutes)
     */
    @Query("SELECT d FROM PushDevice d WHERE d.lastUsedAt > :since AND d.isActive = true")
    List<PushDevice> findRecentlyActive(@Param("since") LocalDateTime since);

    /**
     * Find devices active in last N days
     */
    default List<PushDevice> findActiveInLastDays(int days) {
        LocalDateTime since = LocalDateTime.now().minusDays(days);
        return findRecentlyActive(since);
    }

    // ==================== Count Methods ====================

    /**
     * Count active devices for a user
     */
    long countByUserIdAndIsActiveTrue(String userId);

    /**
     * Count total devices for a user
     */
    long countByUserId(String userId);

    /**
     * Count devices by platform
     */
    long countByPlatform(String platform);

    /**
     * Count active devices by platform
     */
    long countByPlatformAndIsActiveTrue(String platform);

    /**
     * Get total active devices count
     */
    @Query("SELECT COUNT(d) FROM PushDevice d WHERE d.isActive = true")
    long countActiveDevices();

    // ==================== Update Methods ====================

    /**
     * Update last used timestamp for a device
     */
    @Modifying
    @Transactional
    @Query("UPDATE PushDevice d SET d.lastUsedAt = :now WHERE d.deviceToken = :deviceToken")
    int updateLastUsedByToken(@Param("deviceToken") String deviceToken,
                              @Param("now") LocalDateTime now);

    /**
     * Update last used timestamp for a device by ID
     */
    @Modifying
    @Transactional
    @Query("UPDATE PushDevice d SET d.lastUsedAt = :now WHERE d.id = :deviceId")
    int updateLastUsedById(@Param("deviceId") String deviceId,
                           @Param("now") LocalDateTime now);

    /**
     * Update app version for a device
     */
    @Modifying
    @Transactional
    @Query("UPDATE PushDevice d SET d.appVersion = :appVersion WHERE d.deviceToken = :deviceToken")
    int updateAppVersion(@Param("deviceToken") String deviceToken,
                         @Param("appVersion") String appVersion);

    // ==================== Deactivation Methods ====================

    /**
     * Deactivate a device by token
     */
    @Modifying
    @Transactional
    @Query("UPDATE PushDevice d SET d.isActive = false WHERE d.deviceToken = :deviceToken")
    int deactivateByToken(@Param("deviceToken") String deviceToken);

    /**
     * Deactivate a device by ID
     */
    @Modifying
    @Transactional
    @Query("UPDATE PushDevice d SET d.isActive = false WHERE d.id = :deviceId")
    int deactivateById(@Param("deviceId") String deviceId);

    /**
     * Deactivate all devices for a user
     */
    @Modifying
    @Transactional
    @Query("UPDATE PushDevice d SET d.isActive = false WHERE d.userId = :userId")
    int deactivateAllForUser(@Param("userId") String userId);

    /**
     * Deactivate devices older than cutoff
     */
    @Modifying
    @Transactional
    @Query("UPDATE PushDevice d SET d.isActive = false WHERE d.lastUsedAt < :cutoff")
    int deactivateOlderThan(@Param("cutoff") LocalDateTime cutoff);

    // ==================== Delete Methods ====================

    /**
     * Delete device by token
     */
    @Modifying
    @Transactional
    @Query("DELETE FROM PushDevice d WHERE d.deviceToken = :deviceToken")
    int deleteByDeviceToken(@Param("deviceToken") String deviceToken);

    /**
     * Delete all devices for a user
     */
    @Modifying
    @Transactional
    @Query("DELETE FROM PushDevice d WHERE d.userId = :userId")
    int deleteByUserId(@Param("userId") String userId);

    /**
     * Delete inactive devices older than cutoff
     */
    @Modifying
    @Transactional
    @Query("DELETE FROM PushDevice d WHERE d.isActive = false AND d.lastUsedAt < :cutoff")
    int deleteInactiveOlderThan(@Param("cutoff") LocalDateTime cutoff);

    // ==================== Existence Checks ====================

    /**
     * Check if device token exists
     */
    boolean existsByDeviceToken(String deviceToken);

    /**
     * Check if user has any active devices
     */
    @Query("SELECT CASE WHEN COUNT(d) > 0 THEN true ELSE false END FROM PushDevice d " +
            "WHERE d.userId = :userId AND d.isActive = true")
    boolean hasActiveDevices(@Param("userId") String userId);

    // ==================== Bulk Operations ====================

    /**
     * Find all expired tokens that need cleanup
     */
    @Query("SELECT d.deviceToken FROM PushDevice d WHERE d.isActive = false AND d.updatedAt < :cutoff")
    List<String> findExpiredTokens(@Param("cutoff") LocalDateTime cutoff);

    /**
     * Bulk update multiple devices
     */
    @Modifying
    @Transactional
    @Query("UPDATE PushDevice d SET d.isActive = false WHERE d.deviceToken IN :tokens")
    int bulkDeactivate(@Param("tokens") List<String> tokens);

    // ==================== Statistics ====================

    /**
     * Get device count by platform
     */
    @Query("SELECT d.platform, COUNT(d) FROM PushDevice d WHERE d.isActive = true GROUP BY d.platform")
    List<Object[]> getDeviceCountByPlatform();

    /**
     * Get device count by app version
     */
    @Query("SELECT d.appVersion, COUNT(d) FROM PushDevice d WHERE d.isActive = true GROUP BY d.appVersion")
    List<Object[]> getDeviceCountByAppVersion();

    /**
     * Get registration trends by date
     */
    @Query("SELECT DATE(d.createdAt), COUNT(d) FROM PushDevice d " +
            "WHERE d.createdAt BETWEEN :start AND :end GROUP BY DATE(d.createdAt)")
    List<Object[]> getRegistrationTrends(@Param("start") LocalDateTime start,
                                         @Param("end") LocalDateTime end);
    @Transactional
    @Query("UPDATE PushDevice d SET d.lastUsedAt = :lastUsed WHERE d.id = :deviceId")
    int updateLastUsed(@Param("deviceId") String deviceId,
                       @Param("lastUsed") LocalDateTime lastUsed);
}
