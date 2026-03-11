package com.vyxentra.vehicle.repository;

import com.vyxentra.vehicle.entity.Booking;
import com.vyxentra.vehicle.enums.BookingStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface BookingRepository extends JpaRepository<Booking, String>,
        JpaSpecificationExecutor<Booking> {

    Optional<Booking> findByBookingNumber(String bookingNumber);

    List<Booking> findByCustomerIdOrderByCreatedAtDesc(String customerId);

    List<Booking> findByProviderIdOrderByScheduledTimeDesc(String providerId);

    List<Booking> findByEmployeeIdOrderByScheduledTimeDesc(String employeeId);

    List<Booking> findByCustomerIdAndStatusIn(String customerId, List<BookingStatus> statuses);

    List<Booking> findByProviderIdAndStatusIn(String providerId, List<BookingStatus> statuses);

    @Query("SELECT b FROM Booking b WHERE b.customerId = :customerId " +
            "AND (:status IS NULL OR b.status = :status) " +
            "AND (:fromDate IS NULL OR b.createdAt >= :fromDate) " +
            "AND (:toDate IS NULL OR b.createdAt <= :toDate)")
    List<Booking> findCustomerBookings(@Param("customerId") String customerId,
                                       @Param("status") BookingStatus status,
                                       @Param("fromDate") LocalDateTime fromDate,
                                       @Param("toDate") LocalDateTime toDate);

    @Query("SELECT b FROM Booking b WHERE b.providerId = :providerId " +
            "AND (:status IS NULL OR b.status = :status) " +
            "AND (:fromDate IS NULL OR b.createdAt >= :fromDate) " +
            "AND (:toDate IS NULL OR b.createdAt <= :toDate)")
    List<Booking> findProviderBookings(@Param("providerId") String providerId,
                                       @Param("status") BookingStatus status,
                                       @Param("fromDate") LocalDateTime fromDate,
                                       @Param("toDate") LocalDateTime toDate);

    @Query("SELECT b FROM Booking b WHERE b.scheduledTime BETWEEN :start AND :end " +
            "AND b.status = 'CONFIRMED'")
    List<Booking> findConfirmedBookingsInTimeRange(@Param("start") LocalDateTime start,
                                                   @Param("end") LocalDateTime end);

    @Query("SELECT b FROM Booking b WHERE b.createdAt < :expiryTime " +
            "AND b.status IN ('PENDING_PAYMENT', 'PENDING_CONFIRMATION')")
    List<Booking> findExpiredBookings(@Param("expiryTime") LocalDateTime expiryTime);

    @Query("SELECT b FROM Booking b WHERE b.customerId = :customerId " +
            "AND b.scheduledTime > :now AND b.status NOT IN ('CANCELLED', 'COMPLETED', 'EXPIRED')")
    List<Booking> findUpcomingCustomerBookings(@Param("customerId") String customerId,
                                               @Param("now") LocalDateTime now);

    @Query("SELECT b FROM Booking b WHERE b.providerId = :providerId " +
            "AND b.scheduledTime > :now AND b.status NOT IN ('CANCELLED', 'COMPLETED', 'EXPIRED')")
    List<Booking> findUpcomingProviderBookings(@Param("providerId") String providerId,
                                               @Param("now") LocalDateTime now);

    @Modifying
    @Query("UPDATE Booking b SET b.status = :status WHERE b.id = :bookingId")
    void updateStatus(@Param("bookingId") String bookingId, @Param("status") BookingStatus status);

    @Modifying
    @Query("UPDATE Booking b SET b.employeeId = :employeeId WHERE b.id = :bookingId")
    void assignEmployee(@Param("bookingId") String bookingId, @Param("employeeId") String employeeId);

    @Modifying
    @Query("UPDATE Booking b SET b.rating = :rating, b.review = :review WHERE b.id = :bookingId")
    void updateRating(@Param("bookingId") String bookingId,
                      @Param("rating") Integer rating,
                      @Param("review") String review);
}
