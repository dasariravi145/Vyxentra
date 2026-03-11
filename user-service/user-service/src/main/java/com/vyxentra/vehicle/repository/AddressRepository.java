package com.vyxentra.vehicle.repository;

import com.vyxentra.vehicle.entity.Address;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface AddressRepository extends JpaRepository<Address, String> {

    // ✅ FIXED: Must return Page<Address>
    Page<Address> findByUserId(String userId, Pageable pageable);

    Optional<Address> findByIdAndUserId(String id, String userId);

    @Query("SELECT a FROM Address a WHERE a.user.id = :userId AND a.isDefault = true")
    Optional<Address> findDefaultAddressByUserId(@Param("userId") String userId);

    @Modifying
    @Query("UPDATE Address a SET a.isDefault = false WHERE a.user.id = :userId")
    void resetDefaultAddresses(@Param("userId") String userId);

    @Modifying
    @Query("UPDATE Address a SET a.isDefault = true WHERE a.id = :addressId AND a.user.id = :userId")
    void setAsDefault(@Param("addressId") String addressId,
                      @Param("userId") String userId);

    long countByUserId(String userId);
}