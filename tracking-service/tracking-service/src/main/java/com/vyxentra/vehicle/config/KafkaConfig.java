package com.vyxentra.vehicle.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaConfig {

    @Bean
    public NewTopic locationUpdatesTopic() {
        return TopicBuilder.name("location.updates")
                .partitions(5)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic etaUpdatesTopic() {
        return TopicBuilder.name("eta.updates")
                .partitions(3)
                .replicas(1)
                .build();
    }
}
