package com.vyxentra.vehicle.dto.response;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationPreferenceResponse {

    private String userId;
    private Boolean emailEnabled;
    private Boolean smsEnabled;
    private Boolean pushEnabled;
    private Boolean marketingEnabled;
    private Boolean bookingUpdates;
    private Boolean paymentUpdates;
    private Boolean emergencyAlerts;
    private Boolean promotionalOffers;
    private LocalTime quietHoursStart;
    private LocalTime quietHoursEnd;
    private Boolean quietHoursEnabled;
    private LocalTime createdAt;
    private LocalTime updatedAt;
}
