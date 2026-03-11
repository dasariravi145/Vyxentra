package com.vyxentra.vehicle.repository;


import com.vyxentra.vehicle.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, String> {

    Optional<User> findByPhoneNumber(String phoneNumber);

    Optional<User> findByEmail(String email);

    boolean existsByPhoneNumber(String phoneNumber);

    boolean existsByEmail(String email);

    @Modifying
    @Query("UPDATE User u SET u.totalBookings = COALESCE(u.totalBookings, 0) + 1 WHERE u.id = :userId")
    void incrementBookingCount(@Param("userId") String userId);

    @Modifying
    @Query("UPDATE User u SET u.totalSpent = COALESCE(u.totalSpent, 0) + :amount WHERE u.id = :userId")
    void addToTotalSpent(@Param("userId") String userId, @Param("amount") Integer amount);

    @Modifying
    @Query("UPDATE User u SET u.lastLoginAt = CURRENT_TIMESTAMP WHERE u.id = :userId")
    void updateLastLogin(@Param("userId") String userId);

    @Modifying
    @Query("UPDATE User u SET u.active = false WHERE u.id = :userId")
    void deactivateUser(@Param("userId") String userId);
}