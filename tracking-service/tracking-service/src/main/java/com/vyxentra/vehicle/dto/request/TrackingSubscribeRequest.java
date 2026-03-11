package com.vyxentra.vehicle.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TrackingSubscribeRequest {

    @NotBlank(message = "Booking ID is required")
    private String bookingId;

    private Boolean receiveLocationUpdates = true;

    private Boolean receiveETAUpdates = true;

    private Integer updateFrequencySeconds; // For clients that want less frequent updates
}
