package com.vyxentra.vehicle.dto.response;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuditLogResponse {

    private String logId;
    private String eventType;
    private String serviceName;
    private String userId;
    private String userType;
    private String resourceType;
    private String resourceId;
    private String action;
    private String httpMethod;
    private String httpPath;
    private Integer httpStatus;
    private String ipAddress;
    private String userAgent;
    private Long durationMs;
    private Boolean success;
    private String errorMessage;
    private LocalDateTime createdAt;
}
