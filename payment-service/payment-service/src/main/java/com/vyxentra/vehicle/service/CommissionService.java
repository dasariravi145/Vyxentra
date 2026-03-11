package com.vyxentra.vehicle.service;


public interface CommissionService {

    double calculateCommission(double amount, String bookingId);

    double getCommissionPercentage(String providerType);
}
