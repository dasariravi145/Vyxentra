package com.vyxentra.vehicle.dto.response;


import com.vyxentra.vehicle.enums.BookingStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookingTimelineResponse {

    private String timelineId;
    private String bookingId;
    private BookingStatus status;
    private String notes;
    private String changedBy;
    private LocalDateTime changedAt;
}
