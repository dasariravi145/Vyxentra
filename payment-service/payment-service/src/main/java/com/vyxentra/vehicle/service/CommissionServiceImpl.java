package com.vyxentra.vehicle.service;

import com.vyxentra.vehicle.client.BookingServiceClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class CommissionServiceImpl implements CommissionService {

    private final BookingServiceClient bookingServiceClient;

    @Value("${payment.commission.service-center:15}")
    private int serviceCenterCommission;

    @Value("${payment.commission.washing-center:10}")
    private int washingCenterCommission;

    @Value("${payment.commission.minimum:10}")
    private int minimumCommission;

    @Override
    public double calculateCommission(double amount, String bookingId) {
        // Get booking details to determine provider type
        // This would call booking service
        String providerType = getProviderTypeFromBooking(bookingId);

        int percentage = (int) getCommissionPercentage(providerType);
        double commission = amount * percentage / 100;

        // Ensure minimum commission
        return Math.max(commission, minimumCommission);
    }

    @Override
    public double getCommissionPercentage(String providerType) {
        if ("SERVICE_CENTER".equals(providerType)) {
            return serviceCenterCommission;
        } else if ("WASHING_CENTER".equals(providerType)) {
            return washingCenterCommission;
        }
        return serviceCenterCommission; // default
    }

    private String getProviderTypeFromBooking(String bookingId) {
        // In real implementation, call booking service
        // For now, return default
        return "SERVICE_CENTER";
    }
}
