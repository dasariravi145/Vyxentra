package com.vyxentra.vehicle.config;


import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

@Configuration
@EnableCaching
public class CacheConfig {

    @Bean
    public CacheManager cacheManager(RedisConnectionFactory connectionFactory) {
        RedisCacheConfiguration defaultConfig = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofHours(1))
                .disableCachingNullValues()
                .serializeKeysWith(
                        RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer())
                )
                .serializeValuesWith(
                        RedisSerializationContext.SerializationPair.fromSerializer(
                                new GenericJackson2JsonRedisSerializer())
                );

        // Cache-specific configurations
        Map<String, RedisCacheConfiguration> cacheConfigurations = new HashMap<>();

        cacheConfigurations.put("allServices", defaultConfig.entryTtl(Duration.ofMinutes(30)));
        cacheConfigurations.put("servicesPaginated", defaultConfig.entryTtl(Duration.ofMinutes(15)));
        cacheConfigurations.put("serviceDetails", defaultConfig.entryTtl(Duration.ofHours(2)));
        cacheConfigurations.put("serviceByType", defaultConfig.entryTtl(Duration.ofHours(2)));
        cacheConfigurations.put("popularServices", defaultConfig.entryTtl(Duration.ofHours(1)));
        cacheConfigurations.put("recommendedServices", defaultConfig.entryTtl(Duration.ofHours(1)));
        cacheConfigurations.put("servicesByVehicle", defaultConfig.entryTtl(Duration.ofMinutes(30)));
        cacheConfigurations.put("categories", defaultConfig.entryTtl(Duration.ofHours(1)));
        cacheConfigurations.put("category", defaultConfig.entryTtl(Duration.ofHours(1)));
        cacheConfigurations.put("servicesByCategory", defaultConfig.entryTtl(Duration.ofMinutes(30)));
        cacheConfigurations.put("addonsForService", defaultConfig.entryTtl(Duration.ofHours(1)));
        cacheConfigurations.put("addon", defaultConfig.entryTtl(Duration.ofHours(1)));

        return RedisCacheManager.builder(connectionFactory)
                .cacheDefaults(defaultConfig)
                .withInitialCacheConfigurations(cacheConfigurations)
                .build();
    }
}
