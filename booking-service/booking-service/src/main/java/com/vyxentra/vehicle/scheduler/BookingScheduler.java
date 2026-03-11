package com.vyxentra.vehicle.scheduler;



import com.vyxentra.vehicle.service.BookingService;
import com.vyxentra.vehicle.service.DamageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class BookingScheduler {

    private final BookingService bookingService;
    private final DamageService damageService;

    @Scheduled(fixedDelayString = "${booking.scheduler.expiry-interval:300000}")
    public void processExpiredBookings() {
        log.info("Running scheduled job: process expired bookings");
        try {
            bookingService.processExpiredBookings();
        } catch (Exception e) {
            log.error("Error processing expired bookings", e);
        }
    }

    @Scheduled(fixedDelayString = "${booking.scheduler.damage-expiry-interval:3600000}")
    public void processExpiredDamageReports() {
        log.info("Running scheduled job: process expired damage reports");
        try {
            damageService.processExpiredDamageReports();
        } catch (Exception e) {
            log.error("Error processing expired damage reports", e);
        }
    }

    @Scheduled(cron = "${booking.scheduler.reminder-cron:0 0 8 * * *}")
    public void sendBookingReminders() {
        log.info("Running scheduled job: send booking reminders");
    }
}