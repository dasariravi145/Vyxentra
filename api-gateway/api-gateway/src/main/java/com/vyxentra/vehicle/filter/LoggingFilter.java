package com.vyxentra.vehicle.filter;


import com.vyxentra.vehicle.utils.CorrelationIdUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.time.Instant;

@Slf4j
@Component
public class LoggingFilter implements GlobalFilter, Ordered {

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        Instant startTime = Instant.now();

        // Log request
        log.info("Incoming Request: {} {} - CorrelationID: {}",
                request.getMethod(),
                request.getURI().getPath(),
                CorrelationIdUtil.getCurrentCorrelationId());

        return chain.filter(exchange)
                .doFinally(signalType -> {
                    // Log response
                    long duration = Instant.now().toEpochMilli() - startTime.toEpochMilli();
                    log.info("Response Status: {} - Duration: {}ms - CorrelationID: {}",
                            exchange.getResponse().getStatusCode(),
                            duration,
                            CorrelationIdUtil.getCurrentCorrelationId());
                });
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE + 1;
    }
}