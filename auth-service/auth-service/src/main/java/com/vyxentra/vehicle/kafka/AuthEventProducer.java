package com.vyxentra.vehicle.kafka;


import com.vyxentra.vehicle.constants.ServiceConstants;
import com.vyxentra.vehicle.enums.ProviderStatus;
import com.vyxentra.vehicle.event.UserEvent;
import com.vyxentra.vehicle.events.BaseEvent;
import com.vyxentra.vehicle.events.ProviderEvent;
import com.vyxentra.vehicle.utils.CorrelationIdUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;

@Slf4j
@Component
@RequiredArgsConstructor
public class AuthEventProducer {

    private final KafkaTemplate<String, BaseEvent> kafkaTemplate;

    public void publishUserRegistered(String userId, String phoneNumber, String role) {
        UserEvent event = UserEvent.builder()
                .userId(userId)
                .phoneNumber(phoneNumber)
                .role(role)
                .eventType("USER_REGISTERED")
                .correlationId(CorrelationIdUtil.getCurrentCorrelationId())
                .source("auth-service")
                .build();

        publishEvent("user.registered", event);
    }

    public void publishProviderRegistered(String providerId, String businessName,
                                          boolean supportsBike, boolean supportsCar) {
        ProviderEvent event = ProviderEvent.builder()
                .providerId(providerId)
                .businessName(businessName)
                .supportsBike(supportsBike)
                .supportsCar(supportsCar)
                .status(ProviderStatus.valueOf("PENDING_APPROVAL"))
                .eventType("PROVIDER_REGISTERED")
                .correlationId(CorrelationIdUtil.getCurrentCorrelationId())
                .source("auth-service")
                .build();

        publishEvent(ServiceConstants.PROVIDER_APPROVED_TOPIC, event);
    }

    public void publishUserLoggedIn(String userId, String ipAddress) {
        UserEvent event = UserEvent.builder()
                .userId(userId)
                .ipAddress(ipAddress)
                .eventType("USER_LOGGED_IN")
                .correlationId(CorrelationIdUtil.getCurrentCorrelationId())
                .source("auth-service")
                .build();

        publishEvent("user.loggedin", event);
    }

    private void publishEvent(String topic, BaseEvent event) {
        CompletableFuture<SendResult<String, BaseEvent>> future = kafkaTemplate.send(topic, event);

        future.whenComplete((result, ex) -> {
            if (ex == null) {
                log.debug("Event published successfully to {}: {}", topic, event.getEventId());
            } else {
                log.error("Failed to publish event to {}: {}", topic, ex.getMessage());
            }
        });
    }
}
