package com.vyxentra.vehicle.config;


import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaConfig {

    @Bean
    public NewTopic paymentSuccessTopic() {
        return TopicBuilder.name("payment.success")
                .partitions(3)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic paymentFailedTopic() {
        return TopicBuilder.name("payment.failed")
                .partitions(3)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic paymentCreatedTopic() {
        return TopicBuilder.name("payment.created")
                .partitions(3)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic refundProcessedTopic() {
        return TopicBuilder.name("refund.processed")
                .partitions(3)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic payoutProcessedTopic() {
        return TopicBuilder.name("payout.processed")
                .partitions(3)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic walletTransactionTopic() {
        return TopicBuilder.name("wallet.transaction")
                .partitions(3)
                .replicas(1)
                .build();
    }
}
