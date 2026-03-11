package com.vyxentra.vehicle.dto.request;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationPreferenceRequest {

    private Boolean emailEnabled;
    private Boolean smsEnabled;
    private Boolean pushEnabled;
    private Boolean marketingEnabled;
    private Boolean bookingUpdates;
    private Boolean paymentUpdates;
    private Boolean emergencyAlerts;
    private Boolean promotionalOffers;
    private String quietHoursStart;
    private String quietHoursEnd;
    private Boolean quietHoursEnabled;
}
