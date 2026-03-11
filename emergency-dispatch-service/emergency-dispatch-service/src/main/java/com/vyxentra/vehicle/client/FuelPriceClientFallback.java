package com.vyxentra.vehicle.client;


import com.vyxentra.vehicle.dto.response.FuelPriceResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.time.LocalDate;

@Slf4j
@Component
public class FuelPriceClientFallback {

    private static final double DEFAULT_PETROL_PRICE = 102.50;
    private static final double DEFAULT_DIESEL_PRICE = 94.50;

    public Mono<FuelPriceResponse> getFuelPrice(String fuelType, String city) {
        log.error("Fallback: Unable to fetch fuel price for {} in {}", fuelType, city);

        double price = "PETROL".equalsIgnoreCase(fuelType) ? DEFAULT_PETROL_PRICE : DEFAULT_DIESEL_PRICE;

        return Mono.just(FuelPriceResponse.builder()
                .fuelType(fuelType)
                .price(price)
                .city(city)
                .date(LocalDate.now())
                .currency("INR")
                .source("FALLBACK")
                .build());
    }

    public Mono<FuelPriceResponse> getFuelPriceBlocking(String fuelType, String city) {
        return getFuelPrice(fuelType, city);
    }
}
