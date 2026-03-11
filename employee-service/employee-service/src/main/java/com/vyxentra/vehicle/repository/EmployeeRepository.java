package com.vyxentra.vehicle.repository;

import com.vyxentra.vehicle.entity.Employee;
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
public interface EmployeeRepository extends JpaRepository<Employee, String> {

    Optional<Employee> findByUserId(String userId);

    Optional<Employee> findByEmployeeCode(String employeeCode);

    List<Employee> findByProviderId(String providerId);

    Page<Employee> findByProviderId(String providerId, Pageable pageable);

    List<Employee> findByProviderIdAndStatus(String providerId, String status);

    @Query("SELECT e FROM Employee e WHERE e.providerId = :providerId AND e.status = 'ACTIVE'")
    List<Employee> findActiveByProviderId(@Param("providerId") String providerId);

    @Query("SELECT e FROM Employee e WHERE e.id IN (" +
            "SELECT a.employee.id FROM Assignment a WHERE a.bookingId = :bookingId)")
    Optional<Employee> findByBookingId(@Param("bookingId") String bookingId);

    boolean existsByEmployeeCode(String employeeCode);

    boolean existsByUserId(String userId);

    @Modifying
    @Query("UPDATE Employee e SET e.totalServicesCompleted = COALESCE(e.totalServicesCompleted, 0) + 1 " +
            "WHERE e.id = :employeeId")
    void incrementServicesCompleted(@Param("employeeId") String employeeId);

    @Modifying
    @Query("UPDATE Employee e SET e.averageRating = :rating WHERE e.id = :employeeId")
    void updateRating(@Param("employeeId") String employeeId, @Param("rating") Double rating);
}
