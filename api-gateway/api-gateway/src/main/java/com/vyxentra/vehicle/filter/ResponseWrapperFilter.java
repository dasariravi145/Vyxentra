package com.vyxentra.vehicle.filter;

import lombok.extern.slf4j.Slf4j;
import org.reactivestreams.Publisher;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.http.server.reactive.ServerHttpResponseDecorator;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;


import java.nio.charset.StandardCharsets;

@Slf4j
@Component
public class ResponseWrapperFilter extends AbstractGatewayFilterFactory<ResponseWrapperFilter.Config> {

    public ResponseWrapperFilter() {
        super(Config.class);
    }

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            ServerHttpResponse originalResponse = exchange.getResponse();

            ServerHttpResponseDecorator decoratedResponse = new ServerHttpResponseDecorator(originalResponse) {
                @Override
                public Mono<Void> writeWith(Publisher<? extends DataBuffer> body) {
                    if (body instanceof Flux) {
                        Flux<? extends DataBuffer> fluxBody = Flux.from(body);

                        return super.writeWith(fluxBody.buffer().map(dataBuffers -> {
                            StringBuilder responseBody = new StringBuilder();
                            dataBuffers.forEach(buffer -> {
                                byte[] content = new byte[buffer.readableByteCount()];
                                buffer.read(content);
                                DataBufferUtils.release(buffer);
                                responseBody.append(new String(content, StandardCharsets.UTF_8));
                            });

                            // Add custom headers to response
                            HttpHeaders headers = originalResponse.getHeaders();
                            headers.add("X-Response-Time", String.valueOf(System.currentTimeMillis()));

                            if (config.isLogResponse() && !responseBody.isEmpty()) {
                                log.debug("Response Body: {}", responseBody);
                            }

                            // Return modified response if needed
                            return bufferFactory().wrap(responseBody.toString().getBytes(StandardCharsets.UTF_8));
                        }));
                    }
                    return super.writeWith(body);
                }
            };

            return chain.filter(exchange.mutate().response(decoratedResponse).build());
        };
    }

    public static class Config {
        private boolean logResponse = false;

        public boolean isLogResponse() {
            return logResponse;
        }

        public void setLogResponse(boolean logResponse) {
            this.logResponse = logResponse;
        }
    }
}
