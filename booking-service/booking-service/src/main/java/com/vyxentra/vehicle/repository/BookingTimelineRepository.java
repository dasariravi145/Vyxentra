package com.vyxentra.vehicle.repository;


import com.vyxentra.vehicle.entity.BookingTimeline;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BookingTimelineRepository extends JpaRepository<BookingTimeline, String> {

    List<BookingTimeline> findByBookingIdOrderByChangedAtDesc(String bookingId);
}