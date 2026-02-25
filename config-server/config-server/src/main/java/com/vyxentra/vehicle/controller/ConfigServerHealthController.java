package com.vyxentra.vehicle.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.cloud.config.server.environment.EnvironmentRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/health")
public class ConfigServerHealthController implements HealthIndicator {

        private static final Logger logger = LoggerFactory.getLogger(ConfigServerHealthController.class);
        @Autowired
        private EnvironmentRepository environmentRepository;
        @Override
        public Health health() {
            Map<String, Object> details = new HashMap<>();
            details.put("service", "config-server");
            details.put("status", "running");
            details.put("timestamp", System.currentTimeMillis());

            try {
                // Test environment repository
                environmentRepository.findOne("application", "default", "");
                details.put("environmentRepository", "available");
                return Health.up().withDetails(details).build();
            } catch (Exception e) {
                logger.error("Health check failed: {}", e.getMessage());
                details.put("error", e.getMessage());
                return Health.down().withDetails(details).build();
            }
        }

        @GetMapping("/status")
        public ResponseEntity<Map<String, Object>> getStatus() {
            Map<String, Object> status = new HashMap<>();
            status.put("service", "config-server");
            status.put("status", "operational");
            status.put("timestamp", System.currentTimeMillis());
            status.put("encryption", environmentRepository.getClass().getSimpleName());

            logger.info("Health status requested: {}", status);
            return ResponseEntity.ok(status);
        }

        @GetMapping("/info")
        public ResponseEntity<Map<String, String>> getInfo() {
            Map<String, String> info = new HashMap<>();
            info.put("name", "Vehicle Service Platform Config Server");
            info.put("version", "1.0.0");
            info.put("java.version", System.getProperty("java.version"));
            info.put("encryption.enabled", "true");

            logger.info("Info endpoint accessed");
            return ResponseEntity.ok(info);
        }

}
