package com.vyxentra.vehicle.service;


import com.vyxentra.vehicle.entity.Booking;
import com.vyxentra.vehicle.enums.BookingStatus;
import com.vyxentra.vehicle.enums.PaymentStatus;
import com.vyxentra.vehicle.exceptions.ResourceNotFoundException;
import com.vyxentra.vehicle.kafka.BookingEventProducer;
import com.vyxentra.vehicle.repository.BookingRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentWebhookService {

    private final BookingRepository bookingRepository;
    private final SnapshotService snapshotService;
    private final BookingEventProducer eventProducer;

    @Transactional
    public void processPaymentSuccess(String bookingId, String paymentId, Double amount) {
        log.info("Processing payment success for booking: {}, payment: {}", bookingId, paymentId);

        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking", bookingId));

        booking.setPaidAmount(amount);
        booking.setPaymentStatus(PaymentStatus.COMPLETED.toString());
        booking.setUpfrontPaid(true);

        // If this was a washing center booking, confirm it
        if (booking.getUpfrontPaymentRequired() && booking.getStatus() == BookingStatus.PENDING_PAYMENT) {
            booking.setStatus(BookingStatus.CONFIRMED);
        }

        bookingRepository.save(booking);
        snapshotService.createSnapshot(booking, "PAYMENT_SUCCESS", "SYSTEM");

        log.info("Payment success processed for booking: {}", bookingId);
    }

    @Transactional
    public void processPaymentFailed(String bookingId, String paymentId, String reason) {
        log.info("Processing payment failure for booking: {}, payment: {}", bookingId, paymentId);

        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking", bookingId));

        booking.setPaymentStatus(PaymentStatus.FAILED.toString());
        bookingRepository.save(booking);

        log.info("Payment failure processed for booking: {}", bookingId);
    }
}
