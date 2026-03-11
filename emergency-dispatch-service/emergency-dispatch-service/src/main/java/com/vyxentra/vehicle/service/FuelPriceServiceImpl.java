package com.vyxentra.vehicle.service;

import com.vyxentra.vehicle.client.FuelPriceClient;
import com.vyxentra.vehicle.client.FuelPriceClientFallback;
import com.vyxentra.vehicle.dto.response.FuelPriceResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
@RequiredArgsConstructor
public class FuelPriceServiceImpl implements FuelPriceService {

    private final FuelPriceClient fuelPriceClient;
    private final FuelPriceClientFallback fallbackClient;

    private final Map<String, FuelPriceResponse> priceCache = new ConcurrentHashMap<>();

    @Override
    @Cacheable(value = "fuelPrice", key = "#fuelType + '-' + #city", unless = "#result == null")
    public Mono<Double> getFuelPrice(String fuelType, String city) {
        return fuelPriceClient.getFuelPrice(fuelType, city)
                .map(FuelPriceResponse::getPrice)
                .timeout(Duration.ofSeconds(3))
                .onErrorResume(error -> {
                    log.warn("Error fetching fuel price, using fallback: {}", error.getMessage());
                    return fallbackClient.getFuelPrice(fuelType, city)
                            .map(FuelPriceResponse::getPrice);
                });
    }

    @Override
    @Cacheable(value = "fuelPriceDetails", key = "#fuelType + '-' + #city")
    public Mono<FuelPriceResponse> getFuelPriceDetails(String fuelType, String city) {
        return fuelPriceClient.getFuelPrice(fuelType, city)
                .timeout(Duration.ofSeconds(3))
                .doOnNext(response -> {
                    String cacheKey = fuelType + "-" + city;
                    priceCache.put(cacheKey, response);
                })
                .onErrorResume(error -> {
                    log.warn("Error fetching fuel price details, using fallback: {}", error.getMessage());
                    return fallbackClient.getFuelPrice(fuelType, city);
                });
    }

    @Override
    public Mono<Map<String, Object>> getFuelPricesByCity(String city) {
        return fuelPriceClient.getFuelPricesByCity(city)
                .timeout(Duration.ofSeconds(5))
                .onErrorResume(error -> {
                    log.error("Failed to fetch fuel prices for city: {}", city, error);
                    return Mono.just(Map.of(
                            "city", city,
                            "error", "Unable to fetch prices",
                            "fallback", true
                    ));
                });
    }

    @Override
    @CacheEvict(value = {"fuelPrice", "fuelPriceDetails"}, allEntries = true)
    public void refreshFuelPrices() {
        log.info("Refreshing fuel price cache");
        priceCache.clear();
    }

    @Override
    public Double getFuelPriceBlocking(String fuelType, String city) {
        try {
            return getFuelPrice(fuelType, city).block(Duration.ofSeconds(5));
        } catch (Exception e) {
            log.error("Blocking fuel price fetch failed: {}", e.getMessage());
            return "PETROL".equalsIgnoreCase(fuelType) ? 102.50 : 94.50;
        }
    }
}