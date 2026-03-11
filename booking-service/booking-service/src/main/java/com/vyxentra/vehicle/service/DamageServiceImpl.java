package com.vyxentra.vehicle.service;

import com.vyxentra.vehicle.dto.DamageItem;
import com.vyxentra.vehicle.dto.request.DamageApprovalRequest;
import com.vyxentra.vehicle.dto.request.DamageItemRequest;
import com.vyxentra.vehicle.dto.request.DamageReportRequest;
import com.vyxentra.vehicle.dto.response.DamageReportResponse;
import com.vyxentra.vehicle.entity.Booking;
import com.vyxentra.vehicle.entity.DamageReport;
import com.vyxentra.vehicle.enums.ApprovalStatus;
import com.vyxentra.vehicle.enums.BookingStatus;
import com.vyxentra.vehicle.exceptions.BusinessException;
import com.vyxentra.vehicle.exceptions.ErrorCode;
import com.vyxentra.vehicle.exceptions.ResourceNotFoundException;
import com.vyxentra.vehicle.kafka.BookingEventProducer;
import com.vyxentra.vehicle.mapper.DamageMapper;
import com.vyxentra.vehicle.repository.BookingRepository;
import com.vyxentra.vehicle.repository.DamageItemRepository;
import com.vyxentra.vehicle.repository.DamageReportRepository;
import com.vyxentra.vehicle.validator.BookingValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class DamageServiceImpl implements DamageService {

    private final DamageReportRepository damageReportRepository;
    private final DamageItemRepository damageItemRepository;
    private final BookingRepository bookingRepository;
    private final DamageMapper damageMapper;
    private final BookingValidator bookingValidator;
    private final SnapshotService snapshotService;
    private final BookingEventProducer eventProducer;

    @Value("${booking.damage.approval-timeout-hours:24}")
    private int approvalTimeoutHours;

    @Value("${booking.damage.auto-approve-threshold:500}")
    private double autoApproveThreshold;

    @Override
    @Transactional
    public DamageReportResponse reportDamage(String employeeId, DamageReportRequest request) {

        Booking booking = bookingRepository.findById(request.getBookingId())
                .orElseThrow(() -> new ResourceNotFoundException("Booking", request.getBookingId()));

        if (!employeeId.equals(booking.getEmployeeId())) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED,
                    "Not authorized to report damage for this booking");
        }

        if (booking.getStatus() != BookingStatus.IN_PROGRESS &&
                booking.getStatus() != BookingStatus.COMPLETED) {
            throw new BusinessException(ErrorCode.BOOKING_INVALID_STATUS,
                    "Damage can only be reported for in-progress or completed bookings");
        }

        // ✅ FIXED: BigDecimal safe summation then convert to double
        BigDecimal totalAmountBD = request.getItems().stream()
                .map(DamageItemRequest::getEstimatedCost)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        double totalAmount = totalAmountBD.doubleValue();

        DamageReport damageReport = DamageReport.builder()
                .booking(booking)
                .reportedBy(employeeId)
                .reportedAt(LocalDateTime.now())
                .status(ApprovalStatus.REPORTED)
                .totalAmount(totalAmount)
                .notes(request.getNotes())
                .images(request.getImages() != null ? request.getImages().toArray(new String[0]) : null)
                .build();

        damageReport = damageReportRepository.save(damageReport);

        for (DamageItemRequest itemRequest : request.getItems()) {
            DamageItem item = DamageItem.builder()
                    .damageReport(damageReport)
                    .itemName(itemRequest.getItemName())
                    .description(itemRequest.getDescription())
                    .estimatedCost(itemRequest.getEstimatedCost())
                    .approved(false)
                    .images(itemRequest.getImages() != null ?
                            List.of(itemRequest.getImages().toArray(new String[0])) : null)
                    .build();
            damageItemRepository.save(item);
        }

        booking.setStatus(BookingStatus.DAMAGE_REPORTED);
        bookingRepository.save(booking);

        snapshotService.createSnapshot(booking, "DAMAGE_REPORTED", employeeId);
        eventProducer.publishDamageReported(booking, damageReport);

        return damageMapper.toResponse(damageReport);
    }

    @Override
    @Transactional(readOnly = true)
    public DamageReportResponse getDamageReport(String reportId) {
        DamageReport damageReport = damageReportRepository.findById(reportId)
                .orElseThrow(() -> new ResourceNotFoundException("DamageReport", reportId));
        return damageMapper.toResponse(damageReport);
    }

    @Override
    @Transactional(readOnly = true)
    public List<DamageReportResponse> getBookingDamageReports(String bookingId) {
        List<DamageReport> reports = damageReportRepository.findByBookingIdOrderByCreatedAtDesc(bookingId);
        return damageMapper.toResponseList(reports);
    }

    @Override
    @Transactional
    public void approveDamage(String reportId, String customerId, DamageApprovalRequest request) {

        DamageReport damageReport = damageReportRepository.findById(reportId)
                .orElseThrow(() -> new ResourceNotFoundException("DamageReport", reportId));

        Booking booking = damageReport.getBooking();

        if (!customerId.equals(booking.getCustomerId())) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED);
        }

        if (!"REPORTED".equals(damageReport.getStatus())) {
            throw new BusinessException(ErrorCode.DAMAGE_ALREADY_APPROVED);
        }

        BigDecimal approvedAmountBD = BigDecimal.ZERO;
        boolean allApproved = true;
        boolean anyApproved = false;

        for (var approvedItem : request.getApprovedItems()) {

            var item = damageItemRepository.findById(approvedItem.getItemId())
                    .orElseThrow(() ->
                            new ResourceNotFoundException("DamageItem", approvedItem.getItemId()));

            if (Boolean.TRUE.equals(approvedItem.getApproved())) {

                BigDecimal cost = approvedItem.getApprovedCost() != null
                        ? approvedItem.getApprovedCost()
                        : item.getEstimatedCost();

                item.setApprovedCost(cost);
                item.setApproved(true);
                item.setApprovedAt(LocalDateTime.now());

                approvedAmountBD = approvedAmountBD.add(cost);
                anyApproved = true;

            } else {
                item.setApproved(false);
                item.setRejectionReason(approvedItem.getRejectionReason());
                allApproved = false;
            }

            damageItemRepository.save(item);
        }

        if (!anyApproved) {
            throw new BusinessException(ErrorCode.APPROVAL_ITEMS_REQUIRED);
        }

        double approvedAmount = approvedAmountBD.doubleValue();

        String status = allApproved ? "APPROVED" : "PARTIALLY_APPROVED";

        damageReport.setStatus(ApprovalStatus.valueOf(status));
        damageReport.setApprovedAmount(approvedAmount);
        damageReport.setApprovedBy(customerId);
        damageReport.setApprovedAt(LocalDateTime.now());
        damageReportRepository.save(damageReport);

        booking.setStatus(BookingStatus.DAMAGE_APPROVED);
        booking.setApprovedAmount(approvedAmount);
        bookingRepository.save(booking);
    }


    @Override
    @Transactional
    public void rejectDamage(String reportId, String customerId, String reason) {
        log.info("Rejecting damage report: {} by customer: {}", reportId, customerId);

        DamageReport damageReport = damageReportRepository.findById(reportId)
                .orElseThrow(() -> new ResourceNotFoundException("DamageReport", reportId));

        Booking booking = damageReport.getBooking();

        // Verify customer owns this booking
        if (!customerId.equals(booking.getCustomerId())) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED, "Not authorized to reject this damage report");
        }

        // Verify report is in REPORTED state
        if (!"REPORTED".equals(damageReport.getStatus())) {
            throw new BusinessException(ErrorCode.DAMAGE_ALREADY_REJECTED,
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
    }

    @Override
    @Transactional(readOnly = true)
    public List<DamageReportResponse> getPendingDamageReports(String customerId) {
        List<DamageReport> reports = damageReportRepository.findPendingForCustomer(customerId);
        return damageMapper.toResponseList(reports);
    }

    @Override
    @Transactional
    public void processExpiredDamageReports() {

        log.info("Processing expired damage reports");

        LocalDateTime timeout = LocalDateTime.now().minusHours(approvalTimeoutHours);
        List<DamageReport> expiredReports =
                damageReportRepository.findExpiredReports(timeout);

        for (DamageReport report : expiredReports) {

            Booking booking = report.getBooking();

            // ✅ Since totalAmount is Double, use normal comparison
            if (report.getTotalAmount() != null &&
                    report.getTotalAmount() <= autoApproveThreshold) {

                // Auto-approve all items
                for (DamageItem item : report.getItems()) {
                    item.setApproved(true);
                    item.setApprovedCost(item.getEstimatedCost());
                    item.setApprovedAt(LocalDateTime.now());
                    damageItemRepository.save(item);
                }

                report.setStatus(ApprovalStatus.APPROVED);
                report.setApprovedAmount(report.getTotalAmount());
                report.setApprovedBy("SYSTEM");
                report.setApprovedAt(LocalDateTime.now());
                damageReportRepository.save(report);

                booking.setStatus(BookingStatus.DAMAGE_APPROVED);
                booking.setApprovedAmount(report.getTotalAmount());
                bookingRepository.save(booking);

                log.info("Damage report auto-approved: {}", report.getId());

            } else {

                report.setStatus(ApprovalStatus.REJECTED);
                report.setRejectionReason("Auto-rejected due to approval timeout");
                report.setApprovedBy("SYSTEM");
                report.setApprovedAt(LocalDateTime.now());
                damageReportRepository.save(report);

                booking.setStatus(BookingStatus.DAMAGE_REJECTED);
                bookingRepository.save(booking);

                log.info("Damage report auto-rejected: {}", report.getId());
            }
        }
    }

}
