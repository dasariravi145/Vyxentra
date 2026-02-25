package com.vyxentra.vehicle.config;

import feign.RequestInterceptor;
import feign.codec.ErrorDecoder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Configuration
public class FeignClientConfig {

    @Bean
    public RequestInterceptor requestInterceptor() {
        return requestTemplate -> {
            ServletRequestAttributes attributes =
                    (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();

            if (attributes != null) {
                String correlationId = attributes.getRequest().getHeader("X-Correlation-ID");
                if (correlationId != null) {
                    requestTemplate.header("X-Correlation-ID", correlationId);
                }

                String requestId = attributes.getRequest().getHeader("X-Request-ID");
                if (requestId != null) {
                    requestTemplate.header("X-Request-ID", requestId);
                }
            }
        };
    }

    @Bean
    public ErrorDecoder errorDecoder() {
        return new FeignErrorDecoder();
    }

    public static class FeignErrorDecoder implements ErrorDecoder {
        private final ErrorDecoder defaultDecoder = new Default();

        @Override
        public Exception decode(String methodKey, feign.Response response) {
            // Custom error handling logic
            return defaultDecoder.decode(methodKey, response);
        }
    }
}

