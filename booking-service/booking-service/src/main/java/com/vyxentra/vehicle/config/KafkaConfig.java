package com.vyxentra.vehicle.config;


import com.vyxentra.vehicle.constants.ServiceConstants;
import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaConfig {

    @Bean
    public NewTopic bookingCreatedTopic() {
        return TopicBuilder.name(ServiceConstants.BOOKING_CREATED_TOPIC)
                .partitions(3)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic emergencyTriggeredTopic() {
        return TopicBuilder.name(ServiceConstants.EMERGENCY_TRIGGERED_TOPIC)
                .partitions(3)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic damageReportedTopic() {
        return TopicBuilder.name(ServiceConstants.DAMAGE_REPORTED_TOPIC)
                .partitions(3)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic damageApprovedTopic() {
        return TopicBuilder.name(ServiceConstants.DAMAGE_APPROVED_TOPIC)
                .partitions(3)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic damageRejectedTopic() {
        return TopicBuilder.name(ServiceConstants.DAMAGE_REJECTED_TOPIC)
                .partitions(3)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic repairDelayedTopic() {
        return TopicBuilder.name(ServiceConstants.REPAIR_DELAYED_TOPIC)
                .partitions(3)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic serviceStartedTopic() {
        return TopicBuilder.name(ServiceConstants.SERVICE_STARTED_TOPIC)
                .partitions(3)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic serviceCompletedTopic() {
        return TopicBuilder.name(ServiceConstants.SERVICE_COMPLETED_TOPIC)
                .partitions(3)
                .replicas(1)
                .build();
    }
}
