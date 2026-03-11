package com.vyxentra.vehicle.config;


import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaConfig {

    @Bean
    public NewTopic userRegisteredTopic() {
        return TopicBuilder.name("user.registered")
                .partitions(3)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic userLoggedInTopic() {
        return TopicBuilder.name("user.loggedin")
                .partitions(3)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic providerRegisteredTopic() {
        return TopicBuilder.name("provider.registered")
                .partitions(3)
                .replicas(1)
                .build();
    }
}