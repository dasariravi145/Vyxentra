package com.vyxentra.vehicle.service;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.opencsv.CSVWriter;
import com.vyxentra.vehicle.dto.request.DateRangeRequest;
import com.vyxentra.vehicle.dto.response.AuditLogResponse;
import com.vyxentra.vehicle.dto.response.PageResponse;
import com.vyxentra.vehicle.entity.AuditLog;
import com.vyxentra.vehicle.repository.AuditLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.io.StringWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuditServiceImpl implements AuditService {

    private final AuditLogRepository auditLogRepository;
    private final ObjectMapper objectMapper;

    @Value("${admin.audit.retention-days:90}")
    private int retentionDays;

    @Override
    @Transactional(readOnly = true)
    public PageResponse<AuditLogResponse> getAuditLogs(DateRangeRequest dateRange, String eventType,
                                                       String userId, String resourceType, Boolean success,
                                                       Pageable pageable) {
        log.debug("Fetching audit logs with filters");

        Page<AuditLog> page = auditLogRepository.findAuditLogs(
                dateRange.getFromDate(),
                dateRange.getToDate(),
                eventType,
                userId,
                resourceType,
                success,
                pageable
        );

        return PageResponse.<AuditLogResponse>builder()
                .content(mapToResponse(page.getContent()))
                .pageNumber(page.getNumber())
                .pageSize(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .first(page.isFirst())
                .last(page.isLast())
                .hasNext(page.hasNext())
                .hasPrevious(page.hasPrevious())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public byte[] exportAuditLogs(DateRangeRequest dateRange, String format) {
        log.info("Exporting audit logs in format: {}", format);

        List<AuditLog> logs = auditLogRepository.findAuditLogs(
                dateRange.getFromDate(),
                dateRange.getToDate(),
                null, null, null, null,
                Pageable.unpaged()
        ).getContent();

        if ("csv".equalsIgnoreCase(format)) {
            return exportToCSV(logs);
        } else {
            return exportToExcel(logs);
        }
    }

    @Override
    @Transactional
    public void cleanupOldAuditLogs() {
        log.info("Cleaning up audit logs older than {} days", retentionDays);

        LocalDateTime cutoffDate = LocalDateTime.now().minusDays(retentionDays);
        int deleted = auditLogRepository.deleteOlderThan(cutoffDate);

        log.info("Deleted {} old audit logs", deleted);
    }

    private byte[] exportToCSV(List<AuditLog> logs) {
        try (StringWriter sw = new StringWriter();
             CSVWriter writer = new CSVWriter(sw)) {

            // Write header
            writer.writeNext(new String[]{
                    "Timestamp", "Event Type", "Service", "User ID", "User Type",
                    "Resource Type", "Resource ID", "Action", "HTTP Method",
                    "Path", "Status", "Duration (ms)", "Success", "Error"
            });

            // Write data
            DateTimeFormatter formatter = DateTimeFormatter.ISO_DATE_TIME;
            for (AuditLog log : logs) {
                writer.writeNext(new String[]{
                        log.getCreatedAt() != null ? log.getCreatedAt().format(formatter) : "",
                        log.getEventType(),
                        log.getServiceName(),
                        log.getUserId(),
                        log.getUserType(),
                        log.getResourceType(),
                        log.getResourceId(),
                        log.getAction(),
                        log.getHttpMethod(),
                        log.getHttpPath(),
                        log.getHttpStatus() != null ? log.getHttpStatus().toString() : "",
                        log.getDurationMs() != null ? log.getDurationMs().toString() : "",
                        log.getSuccess() != null ? log.getSuccess().toString() : "",
                        log.getErrorMessage()
                });
            }

            writer.flush();
            return sw.toString().getBytes();

        } catch (Exception e) {
            log.error("Failed to export audit logs to CSV", e);
            throw new RuntimeException("Export failed", e);
        }
    }

    private byte[] exportToExcel(List<AuditLog> logs) {
        try (Workbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {

            Sheet sheet = workbook.createSheet("Audit Logs");

            // Create header style
            CellStyle headerStyle = workbook.createCellStyle();
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerStyle.setFont(headerFont);

            // Create header row
            Row headerRow = sheet.createRow(0);
            String[] headers = {
                    "Timestamp", "Event Type", "Service", "User ID", "User Type",
                    "Resource Type", "Resource ID", "Action", "HTTP Method",
                    "Path", "Status", "Duration (ms)", "Success", "Error"
            };

            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            // Write data
            DateTimeFormatter formatter = DateTimeFormatter.ISO_DATE_TIME;
            int rowNum = 1;

            for (AuditLog log : logs) {
                Row row = sheet.createRow(rowNum++);

                row.createCell(0).setCellValue(
                        log.getCreatedAt() != null ? log.getCreatedAt().format(formatter) : "");
                row.createCell(1).setCellValue(log.getEventType());
                row.createCell(2).setCellValue(log.getServiceName());
                row.createCell(3).setCellValue(log.getUserId());
                row.createCell(4).setCellValue(log.getUserType());
                row.createCell(5).setCellValue(log.getResourceType());
                row.createCell(6).setCellValue(log.getResourceId());
                row.createCell(7).setCellValue(log.getAction());
                row.createCell(8).setCellValue(log.getHttpMethod());
                row.createCell(9).setCellValue(log.getHttpPath());
                row.createCell(10).setCellValue(
                        log.getHttpStatus() != null ? log.getHttpStatus().toString() : "");
                row.createCell(11).setCellValue(
                        log.getDurationMs() != null ? log.getDurationMs().toString() : "");
                row.createCell(12).setCellValue(
                        log.getSuccess() != null ? log.getSuccess().toString() : "");
                row.createCell(13).setCellValue(log.getErrorMessage());
            }

            // Auto-size columns
            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }

            workbook.write(out);
            return out.toByteArray();

        } catch (Exception e) {
            log.error("Failed to export audit logs to Excel", e);
            throw new RuntimeException("Export failed", e);
        }
    }

    private List<AuditLogResponse> mapToResponse(List<AuditLog> logs) {
        return logs.stream()
                .map(this::mapToResponse)
                .toList();
    }

    private AuditLogResponse mapToResponse(AuditLog log) {
        return AuditLogResponse.builder()
                .logId(log.getId())
                .eventType(log.getEventType())
                .serviceName(log.getServiceName())
                .userId(log.getUserId())
                .userType(log.getUserType())
                .resourceType(log.getResourceType())
                .resourceId(log.getResourceId())
                .action(log.getAction())
                .httpMethod(log.getHttpMethod())
                .httpPath(log.getHttpPath())
                .httpStatus(log.getHttpStatus())
                .ipAddress(log.getIpAddress())
                .userAgent(log.getUserAgent())
                .durationMs(log.getDurationMs())
                .success(log.getSuccess())
                .errorMessage(log.getErrorMessage())
                .createdAt(log.getCreatedAt())
                .build();
    }
}
