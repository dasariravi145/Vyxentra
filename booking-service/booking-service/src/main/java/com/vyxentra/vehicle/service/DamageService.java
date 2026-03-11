package com.vyxentra.vehicle.service;



import com.vyxentra.vehicle.dto.request.DamageApprovalRequest;
import com.vyxentra.vehicle.dto.request.DamageReportRequest;
import com.vyxentra.vehicle.dto.response.DamageReportResponse;

import java.util.List;

public interface DamageService {

    DamageReportResponse reportDamage(String employeeId, DamageReportRequest request);

    DamageReportResponse getDamageReport(String reportId);

    List<DamageReportResponse> getBookingDamageReports(String bookingId);

    void approveDamage(String reportId, String customerId, DamageApprovalRequest request);

    void rejectDamage(String reportId, String customerId, String reason);

    List<DamageReportResponse> getPendingDamageReports(String customerId);

    void processExpiredDamageReports();
}
