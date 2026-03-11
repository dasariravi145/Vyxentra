package com.vyxentra.vehicle.config;



import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaConfig {

    @Bean
    public NewTopic userProfileUpdatedTopic() {
        return TopicBuilder.name("user.profile.updated")
                .partitions(3)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic userDeactivatedTopic() {
        return TopicBuilder.name("user.deactivated")
                .partitions(3)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic addressTopic() {
        return TopicBuilder.name("user.address")
                .partitions(3)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic vehicleTopic() {
        return TopicBuilder.name("user.vehicle")
                .partitions(3)
                .replicas(1)
                .build();
    }
}

