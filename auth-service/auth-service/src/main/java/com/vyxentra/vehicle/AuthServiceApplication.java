package com.vyxentra.vehicle;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.core.env.Environment;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableDiscoveryClient
@EnableFeignClients
@EnableScheduling
public class AuthServiceApplication {

    private static final Logger logger = LoggerFactory.getLogger(AuthServiceApplication.class);

    public static void main(String[] args) {
        SpringApplication app = new SpringApplication(AuthServiceApplication.class);
        Environment env = app.run(args).getEnvironment();

        String port = env.getProperty("server.port", "8081");
        String redisHost = env.getProperty("spring.redis.host", "localhost");

        logger.info("""
                        
                        ----------------------------------------------------------
                        \tAuth Service is running!
                        \tPort: {}
                        \tRedis: {}:{}
                        \tProfile(s): {}
                        \tJWT Expiration: {} minutes
                        ----------------------------------------------------------""",
                port,
                redisHost,
                env.getProperty("spring.redis.port", "6379"),
                env.getActiveProfiles().length > 0 ? String.join(",", env.getActiveProfiles()) : "default",
                env.getProperty("jwt.expiration", "1440"));
    }


    }
