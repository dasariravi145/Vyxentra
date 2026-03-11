package com.vyxentra.vehicle.client;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "provider-service", path = "/api/v1/providers", fallback = ProviderServiceClientFallback.class)
public interface ProviderServiceClient {

    @GetMapping("/{providerId}/validate")
    @CircuitBreaker(name = "providerService")
    Boolean validateProvider(@PathVariable String providerId);
}
