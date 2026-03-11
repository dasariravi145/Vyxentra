package com.vyxentra.vehicle.client;

import org.springframework.stereotype.Component;

@Component
class BookingServiceClientFallback implements BookingServiceClient {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(BookingServiceClientFallback.class);

    @Override
    public Boolean hasActiveBookings(String userId) {
        log.error("Fallback: hasActiveBookings failed for user: {}", userId);
        return false;
    }
}
