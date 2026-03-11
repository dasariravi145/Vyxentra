package com.vyxentra.vehicle.repository;



import com.vyxentra.vehicle.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, String> {

    Optional<User> findByPhoneNumber(String phoneNumber);

    Optional<User> findByEmail(String email);

    boolean existsByPhoneNumber(String phoneNumber);

    boolean existsByEmail(String email);

    @Modifying
    @Query("UPDATE User u SET u.failedAttempts = :attempts WHERE u.phoneNumber = :phoneNumber")
    void updateFailedAttempts(@Param("attempts") int attempts, @Param("phoneNumber") String phoneNumber);

    @Modifying
    @Query("UPDATE User u SET u.accountNonLocked = false, u.lockTime = :lockTime WHERE u.phoneNumber = :phoneNumber")
    void lockUser(@Param("lockTime") Instant lockTime, @Param("phoneNumber") String phoneNumber);

    @Modifying
    @Query("UPDATE User u SET u.failedAttempts = 0, u.accountNonLocked = true, u.lockTime = null WHERE u.phoneNumber = :phoneNumber")
    void resetFailedAttempts(@Param("phoneNumber") String phoneNumber);

    @Modifying
    @Query("UPDATE User u SET u.lastLoginAt = :lastLoginAt, u.lastLoginIp = :ip WHERE u.id = :userId")
    void updateLastLogin(@Param("userId") String userId, @Param("lastLoginAt") Instant lastLoginAt,
                         @Param("ip") String ip);

    @Modifying
    @Query("UPDATE User u SET u.providerStatus = :status, u.approvedBy = :approvedBy, u.approvedAt = :approvedAt WHERE u.id = :userId")
    void updateProviderStatus(@Param("userId") String userId, @Param("status") String status,
                              @Param("approvedBy") String approvedBy, @Param("approvedAt") Instant approvedAt);
}