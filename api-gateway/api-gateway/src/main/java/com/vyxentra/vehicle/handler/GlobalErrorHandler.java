package com.vyxentra.vehicle.handler;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vyxentra.vehicle.dto.response.ErrorResponse;
import jakarta.ws.rs.NotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.reactive.error.ErrorWebExceptionHandler;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpResponse;

import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.time.Instant;

@Slf4j
@Configuration
public class GlobalErrorHandler implements ErrorWebExceptionHandler {

    @Autowired
    private ObjectMapper objectMapper;

    @Override
    public Mono<Void> handle(ServerWebExchange exchange, Throwable ex) {
        ServerHttpResponse response = exchange.getResponse();
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);

        HttpStatus status = determineHttpStatus(ex);
        String message = determineMessage(ex);
        String errorCode = determineErrorCode(ex);

        if (response.isCommitted()) {
            return Mono.error(ex);
        }

        response.setStatusCode(status);

        ErrorResponse errorResponse = ErrorResponse.builder()
                .timestamp(Instant.now().toString())
                .status(status.value())
                .error(errorCode)
                .message(message)
                .path(exchange.getRequest().getPath().value())
                .build();

        log.error("Gateway error - Status: {}, Message: {}, Path: {}",
                status, message, exchange.getRequest().getPath(), ex);

        return response.writeWith(Mono.just(response.bufferFactory()
                .wrap(createErrorResponse(errorResponse))));
    }

    private HttpStatus determineHttpStatus(Throwable ex) {
        if (ex instanceof ResponseStatusException) {
            return (HttpStatus) ((ResponseStatusException) ex).getStatusCode();
        } else if (ex instanceof NotFoundException) {
            return HttpStatus.NOT_FOUND;
        } else {
            return HttpStatus.INTERNAL_SERVER_ERROR;
        }
    }

    private String determineMessage(Throwable ex) {
        if (ex instanceof ResponseStatusException) {
            return ((ResponseStatusException) ex).getReason();
        } else if (ex instanceof NotFoundException) {
            return "Service not found";
        } else {
            return "An unexpected error occurred in the gateway";
        }
    }

    private String determineErrorCode(Throwable ex) {
        if (ex instanceof ResponseStatusException) {
            return "GATEWAY_ERROR";
        } else if (ex instanceof NotFoundException) {
            return "SERVICE_NOT_FOUND";
        } else {
            return "INTERNAL_GATEWAY_ERROR";
        }
    }

    private byte[] createErrorResponse(ErrorResponse errorResponse) {
        try {
            return objectMapper.writeValueAsBytes(errorResponse);
        } catch (JsonProcessingException e) {
            log.error("Failed to create error response", e);
            return "{\"error\":\"Internal server error\"}".getBytes();
        }
    }
}
