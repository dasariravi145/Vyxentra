package com.vyxentra.vehicle.config;


import com.vyxentra.vehicle.constants.ServiceConstants;
import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaConfig {

    @Bean
    public NewTopic emergencyTriggeredTopic() {
        return TopicBuilder.name(ServiceConstants.EMERGENCY_TRIGGERED_TOPIC)
                .partitions(3)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic emergencyAssignedTopic() {
        return TopicBuilder.name("emergency.assigned")
                .partitions(3)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic emergencyCompletedTopic() {
        return TopicBuilder.name("emergency.completed")
                .partitions(3)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic emergencyExpiredTopic() {
        return TopicBuilder.name("emergency.expired")
                .partitions(3)
                .replicas(1)
                .build();
    }
}
