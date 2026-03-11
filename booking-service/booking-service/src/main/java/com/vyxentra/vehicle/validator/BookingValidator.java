package com.vyxentra.vehicle.validator;


import com.vyxentra.vehicle.entity.Booking;
import com.vyxentra.vehicle.enums.BookingStatus;
import com.vyxentra.vehicle.enums.VehicleType;
import com.vyxentra.vehicle.exception.BookingException;
import com.vyxentra.vehicle.exceptions.ErrorCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Slf4j
@Component
public class BookingValidator {

    public void validateProvider(String providerId, VehicleType vehicleType) {
        log.debug("Validating provider: {} for vehicle: {}", providerId, vehicleType);
        // This would call provider service
    }

    public void validateCancellation(Booking booking, String userId) {
        if (!booking.getStatus().equals(BookingStatus.CAN_BECANCELLED)) {
            throw new BookingException(booking.getId(), ErrorCode.BOOKING_INVALID_STATUS,
                    "Booking cannot be cancelled. Current status: " + booking.getStatus());
        }

        if (booking.getScheduledTime().minusHours(2).isBefore(LocalDateTime.now())) {
            throw new BookingException(booking.getId(), ErrorCode.BOOKING_INVALID_STATUS,
                    "Cannot cancel booking within 2 hours of scheduled time");
        }

        String role = userId.startsWith("cust") ? "CUSTOMER" :
                userId.startsWith("prov") ? "PROVIDER" : "ADMIN";

        if ("CUSTOMER".equals(role) && !userId.equals(booking.getCustomerId())) {
            throw new BookingException(booking.getId(), ErrorCode.UNAUTHORIZED);
        }

        if ("PROVIDER".equals(role) && !userId.equals(booking.getProviderId())) {
            throw new BookingException(booking.getId(), ErrorCode.UNAUTHORIZED);
        }
    }

    public void validateDamageApproval(Booking booking, String customerId) {
        if (!customerId.equals(booking.getCustomerId())) {
            throw new BookingException(booking.getId(), ErrorCode.UNAUTHORIZED);
        }

        if (booking.getStatus() != BookingStatus.DAMAGE_REPORTED) {
            throw new BookingException(booking.getId(), ErrorCode.BOOKING_INVALID_STATUS,
                    "Damage can only be approved when booking is in DAMAGE_REPORTED state");
        }
    }

    public void validateBookingForPayment(String bookingId, Double amount) {
        log.debug("Validating booking {} for payment amount {}", bookingId, amount);
    }
}