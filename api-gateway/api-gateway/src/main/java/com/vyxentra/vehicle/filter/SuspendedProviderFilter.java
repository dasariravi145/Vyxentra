package com.vyxentra.vehicle.filter;


import com.vyxentra.vehicle.constants.ServiceConstants;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Slf4j
@Component
public class SuspendedProviderFilter extends AbstractGatewayFilterFactory<SuspendedProviderFilter.Config> {

    private final ReactiveRedisTemplate<String, String> redisTemplate;

    public SuspendedProviderFilter(ReactiveRedisTemplate<String, String> redisTemplate) {
        super(Config.class);
        this.redisTemplate = redisTemplate;
    }

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            String userId = exchange.getRequest().getHeaders().getFirst(ServiceConstants.USER_ID_HEADER);
            String role = exchange.getRequest().getHeaders().getFirst(ServiceConstants.ROLE_HEADER);

            // Only check for providers
            if (!"PROVIDER".equals(role) || userId == null) {
                return chain.filter(exchange);
            }

            String suspendedKey = "suspended:provider:" + userId;

            return redisTemplate.hasKey(suspendedKey)
                    .flatMap(isSuspended -> {
                        if (Boolean.TRUE.equals(isSuspended)) {
                            log.warn("Blocked request from suspended provider: {}", userId);
                            exchange.getResponse().setStatusCode(HttpStatus.FORBIDDEN);
                            return exchange.getResponse().setComplete();
                        }
                        return chain.filter(exchange);
                    });
        };
    }

    public static class Config {
        // Configuration properties if needed
    }
}
