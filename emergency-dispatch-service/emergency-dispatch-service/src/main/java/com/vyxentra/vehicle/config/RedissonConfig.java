package com.vyxentra.vehicle.config;


import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.redisson.config.Config;

@Configuration
public class RedissonConfig {

    @Value("${redisson.single-server-config.address:redis://localhost:6379}")
    private String address;

    @Value("${redisson.single-server-config.password:}")
    private String password;

    @Value("${redisson.single-server-config.connection-pool-size:10}")
    private int connectionPoolSize;

    @Value("${redisson.single-server-config.connection-minimum-idle-size:5}")
    private int connectionMinimumIdleSize;

    @Bean
    public RedissonClient redissonClient() {
        Config config = new Config();

        config.useSingleServer()
                .setAddress(address)
                .setPassword(password.isEmpty() ? null : password)
                .setConnectionPoolSize(connectionPoolSize)
                .setConnectionMinimumIdleSize(connectionMinimumIdleSize);

        return Redisson.create(config);
    }
}
