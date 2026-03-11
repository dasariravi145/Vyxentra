package com.vyxentra.vehicle.repository;

import com.vyxentra.vehicle.entity.Vehicle;
import com.vyxentra.vehicle.enums.VehicleType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface VehicleRepository extends JpaRepository<Vehicle, String> {

    Page<Vehicle> findByUserId(String userId, Pageable pageable);

    Optional<Vehicle> findByIdAndUserId(String id, String userId);

    Optional<Vehicle> findByRegistrationNumber(String registrationNumber);

    boolean existsByRegistrationNumber(String registrationNumber);

    long countByUserId(String userId);

    @Query("SELECT v FROM Vehicle v WHERE v.user.id = :userId AND v.isDefault = true")
    Optional<Vehicle> findDefaultVehicleByUserId(@Param("userId") String userId);

    @Modifying
    @Query("UPDATE Vehicle v SET v.isDefault = false WHERE v.user.id = :userId")
    void resetDefaultVehicles(@Param("userId") String userId);

    @Query("SELECT v FROM Vehicle v WHERE v.user.id = :userId AND v.id != :vehicleId ORDER BY v.createdAt ASC")
    Optional<Vehicle> findFirstByUserIdAndIdNot(@Param("userId") String userId, @Param("vehicleId") String vehicleId);

    List<Vehicle> findByUserIdAndVehicleType(String userId, VehicleType vehicleType);
}