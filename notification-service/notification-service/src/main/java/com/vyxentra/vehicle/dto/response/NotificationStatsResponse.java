package com.vyxentra.vehicle.dto.response;



import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationStatsResponse {

    private Long totalSent;
    private Long totalFailed;
    private Long totalPending;
    private Map<String, Long> sentByChannel;
    private Map<String, Long> sentByType;
    private Double successRate;
    private Double failureRate;
}
