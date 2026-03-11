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
    public NewTopic userUpdatedTopic() {
        return TopicBuilder.name("user.updated")
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
    public NewTopic userVerifiedTopic() {
        return TopicBuilder.name("user.verified")
                .partitions(3)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic userPreferencesUpdatedTopic() {
        return TopicBuilder.name("user.preferences.updated")
                .partitions(3)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic addressAddedTopic() {
        return TopicBuilder.name("address.added")
                .partitions(3)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic addressUpdatedTopic() {
        return TopicBuilder.name("address.updated")
                .partitions(3)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic addressDeletedTopic() {
        return TopicBuilder.name("address.deleted")
                .partitions(3)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic addressDefaultChangedTopic() {
        return TopicBuilder.name("address.default.changed")
                .partitions(3)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic vehicleAddedTopic() {
        return TopicBuilder.name("vehicle.added")
                .partitions(3)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic vehicleUpdatedTopic() {
        return TopicBuilder.name("vehicle.updated")
                .partitions(3)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic vehicleDeletedTopic() {
        return TopicBuilder.name("vehicle.deleted")
                .partitions(3)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic vehicleDefaultChangedTopic() {
        return TopicBuilder.name("vehicle.default.changed")
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
}