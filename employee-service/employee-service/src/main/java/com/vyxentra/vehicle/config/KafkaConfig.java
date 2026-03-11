package com.vyxentra.vehicle.config;



import com.vyxentra.vehicle.constants.ServiceConstants;
import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaConfig {

    @Bean
    public NewTopic employeeRegisteredTopic() {
        return TopicBuilder.name("employee.registered")
                .partitions(3)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic employeeAssignedTopic() {
        return TopicBuilder.name("employee.assigned")
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

    @Bean
    public NewTopic timesheetTopic() {
        return TopicBuilder.name("timesheet")
                .partitions(3)
                .replicas(1)
                .build();
    }
}
