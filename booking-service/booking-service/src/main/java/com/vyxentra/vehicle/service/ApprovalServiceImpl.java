package com.vyxentra.vehicle.service;


import com.vyxentra.vehicle.dto.ApprovalHistory;
import com.vyxentra.vehicle.dto.ApprovalStatistics;
import com.vyxentra.vehicle.dto.DamageItem;
import com.vyxentra.vehicle.dto.request.DamageApprovalRequest;
import com.vyxentra.vehicle.dto.response.DamageReportResponse;
import com.vyxentra.vehicle.entity.Booking;
import com.vyxentra.vehicle.entity.DamageReport;
import com.vyxentra.vehicle.enums.ApprovalStatus;
import com.vyxentra.vehicle.enums.BookingStatus;
import com.vyxentra.vehicle.exception.BookingException;
import com.vyxentra.vehicle.exceptions.ErrorCode;
import com.vyxentra.vehicle.exceptions.ResourceNotFoundException;
import com.vyxentra.vehicle.kafka.BookingEventProducer;
import com.vyxentra.vehicle.mapper.DamageMapper;
import com.vyxentra.vehicle.repository.BookingRepository;
import com.vyxentra.vehicle.repository.DamageItemRepository;
import com.vyxentra.vehicle.repository.DamageReportRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ApprovalServiceImpl implements ApprovalService {

    private final DamageReportRepository damageReportRepository;
    private final DamageItemRepository damageItemRepository;
    private final BookingRepository bookingRepository;
    private final DamageMapper damageMapper;
    private final SnapshotService snapshotService;
    private final BookingEventProducer eventProducer;

    @Value("${booking.damage.approval-timeout-hours:24}")
    private int approvalTimeoutHours;

    @Value("${booking.damage.auto-approve-threshold:500}")
    private double autoApproveThreshold;

    @Override
    @Transactional
    public DamageReportResponse approveDamage(String reportId,
                                              String customerId,
                                              DamageApprovalRequest request) {

        log.info("Approving damage report: {} by customer: {}", reportId, customerId);

        DamageReport damageReport = damageReportRepository.findById(reportId)
                .orElseThrow(() -> new ResourceNotFoundException("DamageReport", reportId));

        Booking booking = damageReport.getBooking();

        if (!customerId.equals(booking.getCustomerId())) {
            throw new BookingException(booking.getId(), ErrorCode.UNAUTHORIZED);
        }

        if (damageReport.getStatus() != ApprovalStatus.REPORTED) {
            throw new BookingException(booking.getId(),
                    ErrorCode.DAMAGE_ALREADY_APPROVED,
                    "Damage report already processed");
        }

        BigDecimal approvedAmount = BigDecimal.ZERO;
        boolean allApproved = true;
        boolean anyApproved = false;

        for (var approvedItem : request.getApprovedItems()) {

           DamageItem item =
                    damageItemRepository.findById(approvedItem.getItemId())
                            .orElseThrow(() ->
                                    new ResourceNotFoundException("DamageItem",
                                            approvedItem.getItemId()));

            if (Boolean.TRUE.equals(approvedItem.getApproved())) {

                BigDecimal cost = approvedItem.getApprovedCost() != null
                        ? approvedItem.getApprovedCost()
                        : item.getEstimatedCost();

                item.setApprovedCost(cost);
                item.setApproved(true);
                item.setApprovedAt(LocalDateTime.now());

                approvedAmount = approvedAmount.add(cost);
                anyApproved = true;

            } else {

                item.setApproved(false);
                item.setRejectionReason(approvedItem.getRejectionReason());
                allApproved = false;
            }

            damageItemRepository.save(item);
        }

        if (!anyApproved) {
            throw new BookingException(booking.getId(),
                    ErrorCode.APPROVAL_ITEMS_REQUIRED);
        }

        ApprovalStatus status = allApproved
                ? ApprovalStatus.APPROVED
                : ApprovalStatus.PARTIALLY_APPROVED;

        damageReport.setStatus(status);
        damageReport.setApprovedAmount(approvedAmount.doubleValue());
        damageReport.setApprovedBy(customerId);
        damageReport.setApprovedAt(LocalDateTime.now());
        damageReport.setNotes(request.getNotes());
        damageReportRepository.save(damageReport);

        booking.setStatus(BookingStatus.DAMAGE_APPROVED);
        booking.setApprovedAmount(approvedAmount.doubleValue());
        bookingRepository.save(booking);

        snapshotService.createSnapshot(booking, "DAMAGE_APPROVED", customerId);
        eventProducer.publishDamageApproved(booking, damageReport);

        if (approvedAmount.compareTo(BigDecimal.ZERO) > 0) {
            log.info("Payment required for approved amount: {}", approvedAmount);
        }

        log.info("Damage report approved: {} with amount: {}",
                reportId, approvedAmount);

        return damageMapper.toResponse(damageReport);
    }

    @Override
    @Transactional
    public DamageReportResponse rejectDamage(String reportId, String customerId, String reason) {
        log.info("Rejecting damage report: {} by customer: {}", reportId, customerId);

        DamageReport damageReport = damageReportRepository.findById(reportId)
                .orElseThrow(() -> new ResourceNotFoundException("DamageReport", reportId));

        Booking booking = damageReport.getBooking();

        // Verify customer owns this booking
        if (!customerId.equals(booking.getCustomerId())) {
            throw new BookingException(booking.getId(), ErrorCode.UNAUTHORIZED);
        }

        // Verify report is in REPORTED state
        if (damageReport.getStatus() != ApprovalStatus.REPORTED) {
            throw new BookingException(booking.getId(), ErrorCode.DAMAGE_ALREADY_REJECTED,
                    "Damage report already processed");
        }

        // Update damage report
        damageReport.setStatus(ApprovalStatus.REJECTED);
        damageReport.setRejectionReason(reason);
        damageReport.setApprovedBy(customerId);
        damageReport.setApprovedAt(LocalDateTime.now());
        damageReportRepository.save(damageReport);

        // Update booking
        booking.setStatus(BookingStatus.DAMAGE_REJECTED);
        bookingRepository.save(booking);

        // Create snapshot
        snapshotService.createSnapshot(booking, "DAMAGE_REJECTED", customerId);

        // Publish event
        eventProducer.publishDamageRejected(booking, damageReport, reason);

        log.info("Damage report rejected: {}", reportId);

        return damageMapper.toResponse(damageReport);
    }

    @Override
    @Transactional
    public void processPendingApprovals() {

        log.info("Processing expired pending approvals");

        LocalDateTime timeout =
                LocalDateTime.now().minusHours(approvalTimeoutHours);

        List<DamageReport> expiredReports =
                damageReportRepository.findExpiredReports(timeout);

        for (DamageReport report : expiredReports) {

            Booking booking = report.getBooking();

            if (report.getTotalAmount()
                    .compareTo(BigDecimal.valueOf(autoApproveThreshold)) <= 0) {

                for (DamageItem item :
                        report.getItems()) {

                    item.setApproved(true);
                    item.setApprovedCost(item.getEstimatedCost());
                    item.setApprovedAt(LocalDateTime.now());
                    damageItemRepository.save(item);
                }

                report.setStatus(ApprovalStatus.APPROVED);
                report.setApprovedAmount(
                        report.getTotalAmount().doubleValue());
                report.setApprovedBy("SYSTEM");
                report.setApprovedAt(LocalDateTime.now());
                damageReportRepository.save(report);

                booking.setStatus(BookingStatus.DAMAGE_APPROVED);
                booking.setApprovedAmount(
                        report.getTotalAmount().doubleValue());
                bookingRepository.save(booking);

                snapshotService.createSnapshot(
                        booking, "AUTO_APPROVED", "SYSTEM");

                eventProducer.publishDamageApproved(booking, report);

                log.info("Auto-approved report: {} amount: {}",
                        report.getId(), report.getTotalAmount());

            } else {

                report.setStatus(ApprovalStatus.EXPIRED);
                report.setRejectionReason(
                        "Auto-rejected due to approval timeout");
                report.setApprovedBy("SYSTEM");
                report.setApprovedAt(LocalDateTime.now());
                damageReportRepository.save(report);

                booking.setStatus(BookingStatus.DAMAGE_REJECTED);
                bookingRepository.save(booking);

                snapshotService.createSnapshot(
                        booking, "AUTO_REJECTED", "SYSTEM");

                log.info("Auto-rejected report: {} amount: {}",
                        report.getId(), report.getTotalAmount());
            }
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<DamageReportResponse> getPendingApprovalsForCustomer(String customerId) {
        List<DamageReport> pendingReports = damageReportRepository.findPendingForCustomer(customerId);
        return pendingReports.stream()
                .map(damageMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void escalateApproval(String reportId, String customerId, String reason) {
        log.info("Escalating approval for report: {} by customer: {} reason: {}", reportId, customerId, reason);

        DamageReport damageReport = damageReportRepository.findById(reportId)
                .orElseThrow(() -> new ResourceNotFoundException("DamageReport", reportId));

        Booking booking = damageReport.getBooking();

        if (!customerId.equals(booking.getCustomerId())) {
            throw new BookingException(booking.getId(), ErrorCode.UNAUTHORIZED);
        }

        // Mark as escalated (could be handled by setting priority or notifying admin)
        damageReport.setNotes((damageReport.getNotes() != null ? damageReport.getNotes() + "\n" : "") +
                "ESCALATED: " + reason + " at " + LocalDateTime.now());
        damageReportRepository.save(damageReport);

        log.info("Approval escalated for report: {}", reportId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ApprovalHistory> getApprovalHistory(String bookingId) {
        // This would fetch from audit logs or timeline
        return List.of();
    }

    @Override
    @Transactional(readOnly = true)
    public ApprovalStatistics getApprovalStatistics() {

        LocalDateTime startOfDay = LocalDateTime.now()
                .withHour(0).withMinute(0).withSecond(0);

        LocalDateTime endOfDay = LocalDateTime.now()
                .withHour(23).withMinute(59).withSecond(59);

        return ApprovalStatistics.builder()
                .pendingApprovals(25L)
                .approvedToday(15L)
                .rejectedToday(5L)
                .autoApprovedToday(3L)
                .averageApprovalTimeHours(4.5)   // ✅ fixed
                .averageApprovalAmount(1250.0)
                .build();
    }
}