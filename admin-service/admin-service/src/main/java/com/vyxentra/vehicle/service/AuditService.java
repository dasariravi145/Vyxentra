package com.vyxentra.vehicle.service;

import com.vyxentra.vehicle.dto.request.DateRangeRequest;
import com.vyxentra.vehicle.dto.response.AuditLogResponse;
import com.vyxentra.vehicle.dto.response.PageResponse;
import org.springframework.data.domain.Pageable;

public interface AuditService {

    PageResponse<AuditLogResponse> getAuditLogs(DateRangeRequest dateRange, String eventType,
                                                String userId, String resourceType, Boolean success,
                                                Pageable pageable);

    byte[] exportAuditLogs(DateRangeRequest dateRange, String format);

    void cleanupOldAuditLogs();
}
