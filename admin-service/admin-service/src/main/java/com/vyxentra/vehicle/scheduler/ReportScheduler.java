package com.vyxentra.vehicle.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ReportScheduler {

    @Scheduled(cron = "${admin.scheduler.daily-report-cron:0 0 1 * * *}") // 1 AM daily
    public void generateDailyReport() {
        log.info("Running scheduled job: generate daily report");
        // Generate and email daily report to admins
    }

    @Scheduled(cron = "${admin.scheduler.weekly-report-cron:0 0 2 * * MON}") // 2 AM every Monday
    public void generateWeeklyReport() {
        log.info("Running scheduled job: generate weekly report");
        // Generate weekly business report
    }

    @Scheduled(cron = "${admin.scheduler.monthly-report-cron:0 0 3 1 * *}") // 3 AM on 1st of month
    public void generateMonthlyReport() {
        log.info("Running scheduled job: generate monthly report");
        // Generate monthly financial report
    }
}