package com.vyxentra.vehicle.repository;


import com.vyxentra.vehicle.entity.BookingSnapshot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BookingSnapshotRepository extends JpaRepository<BookingSnapshot, String> {

    List<BookingSnapshot> findByBookingIdOrderByCreatedAtDesc(String bookingId);

    Optional<BookingSnapshot> findFirstByBookingIdOrderByCreatedAtDesc(String bookingId);

    @Query("SELECT bs FROM BookingSnapshot bs WHERE bs.booking.id = :bookingId AND bs.snapshotType = :type")
    Optional<BookingSnapshot> findByBookingIdAndType(@Param("bookingId") String bookingId,
                                                     @Param("type") String type);
}
