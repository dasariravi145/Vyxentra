package com.vyxentra.vehicle.filter;


import com.vyxentra.vehicle.constants.ServiceConstants;
import com.vyxentra.vehicle.utils.CorrelationIdUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Slf4j
@Component("gatewayCorrelationIdFilter")
public class CorrelationIdFilter implements GlobalFilter, Ordered {

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();

        String correlationId = request.getHeaders().getFirst(ServiceConstants.CORRELATION_ID_HEADER);
        if (correlationId == null || correlationId.isEmpty()) {
            correlationId = UUID.randomUUID().toString();
        }

        // Set correlation ID in ThreadLocal for logging
        CorrelationIdUtil.setCorrelationId(correlationId);

        // Add correlation ID to response headers
        exchange.getResponse().getHeaders().add(ServiceConstants.CORRELATION_ID_HEADER, correlationId);

        // Add correlation ID to request for downstream services
        ServerHttpRequest mutatedRequest = request.mutate()
                .header(ServiceConstants.CORRELATION_ID_HEADER, correlationId)
                .build();

        log.debug("Correlation ID set: {}", correlationId);

        return chain.filter(exchange.mutate().request(mutatedRequest).build())
                .doFinally(signalType -> CorrelationIdUtil.clear());
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE;
    }
}
