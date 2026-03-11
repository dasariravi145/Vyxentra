package com.vyxentra.vehicle.client;


import com.vyxentra.vehicle.dto.response.FuelPriceResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

import java.time.Duration;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class FuelPriceClient {

    private final WebClient fuelPriceWebClient;

    public Mono<FuelPriceResponse> getFuelPrice(String fuelType, String city) {
        log.debug("Fetching fuel price for {} in {}", fuelType, city);

        return fuelPriceWebClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/price")
                        .queryParam("fuelType", fuelType)
                        .queryParam("city", city)
                        .build())
                .retrieve()
                .bodyToMono(FuelPriceResponse.class)
                .retryWhen(Retry.backoff(3, Duration.ofSeconds(1))
                        .filter(throwable -> throwable instanceof WebClientResponseException.TooManyRequests))
                .doOnSuccess(response -> log.debug("Successfully fetched fuel price: {}", response))
                .doOnError(error -> log.error("Failed to fetch fuel price: {}", error.getMessage()))
                .onErrorResume(error -> {
                    log.warn("Using fallback fuel price due to error: {}", error.getMessage());
                    return Mono.just(createFallbackPrice(fuelType, city));
                });
    }

    public Mono<Map<String, Object>> getFuelPricesByCity(String city) {

        return fuelPriceWebClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/prices")
                        .queryParam("city", city)
                        .build())
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {})
                .retryWhen(Retry.backoff(2, Duration.ofSeconds(1)))
                .doOnSuccess(response ->
                        log.debug("Successfully fetched fuel prices for city: {}", city))
                .doOnError(error ->
                        log.error("Failed to fetch fuel prices for city {} : {}", city, error.getMessage()));
    }

    private FuelPriceResponse createFallbackPrice(String fuelType, String city) {
        double defaultPrice = "PETROL".equalsIgnoreCase(fuelType) ? 102.50 : 94.50;

        return FuelPriceResponse.builder()
                .fuelType(fuelType)
                .price(defaultPrice)
                .city(city)
                .date(java.time.LocalDate.now())
                .currency("INR")
                .source("FALLBACK")
                .build();
    }
}
