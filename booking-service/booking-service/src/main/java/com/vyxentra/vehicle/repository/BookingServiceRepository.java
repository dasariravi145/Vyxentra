package com.vyxentra.vehicle.repository;


import com.vyxentra.vehicle.entity.BookingService;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface BookingServiceRepository extends JpaRepository<BookingService, String> {

    List<BookingService> findByBookingId(String bookingId);

    @Modifying
    @Query("UPDATE BookingService bs SET bs.isApproved = :approved, bs.approvedAt = :approvedAt " +
            "WHERE bs.id = :serviceId")
    void updateApprovalStatus(@Param("serviceId") String serviceId,
                              @Param("approved") Boolean approved,
                              @Param("approvedAt") LocalDateTime approvedAt);

    @Modifying
    @Query("UPDATE BookingService bs SET bs.rejectionReason = :reason WHERE bs.id = :serviceId")
    void updateRejectionReason(@Param("serviceId") String serviceId,
                               @Param("reason") String reason);
}
