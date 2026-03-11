package com.vyxentra.vehicle.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaConfig {

    @Bean
    public NewTopic providerRegisteredTopic() {
        return TopicBuilder.name("provider.registered")
                .partitions(3)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic providerApprovedTopic() {
        return TopicBuilder.name("provider.approved")
                .partitions(3)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic providerRejectedTopic() {
        return TopicBuilder.name("provider.rejected")
                .partitions(3)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic providerSuspendedTopic() {
        return TopicBuilder.name("provider.suspended")
                .partitions(3)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic providerActivatedTopic() {
        return TopicBuilder.name("provider.activated")
                .partitions(3)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic providerUpdatedTopic() {
        return TopicBuilder.name("provider.updated")
                .partitions(3)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic pricingUpdatedTopic() {
        return TopicBuilder.name("provider.pricing.updated")
                .partitions(3)
                .replicas(1)
                .build();
    }
}
