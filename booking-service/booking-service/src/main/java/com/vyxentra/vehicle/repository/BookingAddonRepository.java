package com.vyxentra.vehicle.repository;


import com.vyxentra.vehicle.entity.BookingAddon;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BookingAddonRepository extends JpaRepository<BookingAddon, String> {

    List<BookingAddon> findByBookingServiceId(String bookingServiceId);

    List<BookingAddon> findByBookingServiceIdAndIsApprovedTrue(String bookingServiceId);
}
