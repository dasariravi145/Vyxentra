package com.vyxentra.vehicle.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookingSnapshotResponse {

    private String snapshotId;
    private String bookingId;
    private Map<String, Object> snapshotData; // Complete immutable booking data
    private String snapshotType; // INITIAL, PRE_SERVICE, POST_SERVICE, DAMAGE
    private String createdBy;
    private LocalDateTime createdAt;
}