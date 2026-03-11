package com.vyxentra.vehicle.scheduler;

import com.vyxentra.vehicle.entity.ProviderNotification;
import com.vyxentra.vehicle.enums.ProviderResponseStatus;
import com.vyxentra.vehicle.repository.ProviderNotificationRepository;
import com.vyxentra.vehicle.service.EmergencyDispatchService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class ProviderTimeoutScheduler {

    private final ProviderNotificationRepository notificationRepository;
    private final EmergencyDispatchService emergencyDispatchService;

    @Value("${emergency.search.provider-timeout-seconds:30}")
    private int providerTimeoutSeconds;

    @Scheduled(fixedDelayString = "${emergency.scheduler.provider-timeout-interval:10000}") // 10 seconds
    public void processProviderTimeouts() {
        log.debug("Checking for provider response timeouts");

        LocalDateTime timeout = LocalDateTime.now().minusSeconds(providerTimeoutSeconds);
        List<ProviderNotification> expiredNotifications = notificationRepository.findExpiredNotifications(timeout);

        if (expiredNotifications.isEmpty()) {
            return;
        }

        // Group expired notifications by request ID
        Map<String, List<ProviderNotification>> expiredByRequest = expiredNotifications.stream()
                .collect(Collectors.groupingBy(n -> n.getRequest().getId()));

        for (Map.Entry<String, List<ProviderNotification>> entry : expiredByRequest.entrySet()) {
            String requestId = entry.getKey();
            List<ProviderNotification> expired = entry.getValue();

            log.info("Found {} expired provider notifications for request {}", expired.size(), requestId);

            // Mark all as timed out
            for (ProviderNotification notification : expired) {
                notification.setResponseStatus(ProviderResponseStatus.TIMEOUT);
                notification.setRespondedAt(LocalDateTime.now());
                notificationRepository.save(notification);
                log.debug("Provider {} timed out for request {}", notification.getProviderId(), requestId);
            }

            // Check if all providers for this request have responded or timed out
            long pendingCount = notificationRepository.countByRequestIdAndResponseStatus(
                    requestId, ProviderResponseStatus.PENDING);

            if (pendingCount == 0) {
                // All providers have responded or timed out, expand search radius
                log.info("All providers have responded/timed out for request {}, expanding search radius", requestId);
                emergencyDispatchService.expandSearchRadiusForRequest(requestId);
            }
        }
    }
}
