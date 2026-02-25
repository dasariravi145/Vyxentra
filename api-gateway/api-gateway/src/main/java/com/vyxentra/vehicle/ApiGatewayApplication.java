package com.vyxentra.vehicle;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.core.env.Environment;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableDiscoveryClient
@EnableScheduling
public class ApiGatewayApplication {
    private static final Logger logger = LoggerFactory.getLogger(ApiGatewayApplication.class);

	public static void main(String[] args) {
        SpringApplication app = new SpringApplication(ApiGatewayApplication.class);
        Environment env = app.run(args).getEnvironment();

        String protocol = env.getProperty("server.ssl.enabled", "false").equals("true") ? "https" : "http";
        String port = env.getProperty("server.port", "8080");
        logger.info("""
                
                ----------------------------------------------------------
                \tAPI Gateway is running!
                \tAccess URL: {}://localhost:{}
                \tProfile(s): {}
                \tRedis Enabled: {}
                \tRate Limiting Enabled: {}
                \tCircuit Breaker Enabled: {}
                ----------------------------------------------------------""",
                protocol,
                port,
                env.getActiveProfiles().length > 0 ? String.join(",", env.getActiveProfiles()) : "default",
                env.getProperty("spring.redis.host") != null ? "Yes" : "No",
                env.getProperty("spring.cloud.gateway.filter.request-rate-limiter.enabled", "false"),
                "true");
	}

}
