package com.vyxentra.vehicle.dto.request;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateBookingRequest {

    private LocalDateTime scheduledTime;
    private String customerNotes;
    private String providerNotes;
}
