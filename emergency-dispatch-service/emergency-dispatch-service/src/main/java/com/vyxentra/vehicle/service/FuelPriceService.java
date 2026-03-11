package com.vyxentra.vehicle.service;


import com.vyxentra.vehicle.dto.response.FuelPriceResponse;
import reactor.core.publisher.Mono;

import java.util.Map;

public interface FuelPriceService {

    Mono<Double> getFuelPrice(String fuelType, String city);

    Mono<FuelPriceResponse> getFuelPriceDetails(String fuelType, String city);

    Mono<Map<String, Object>> getFuelPricesByCity(String city);

    void refreshFuelPrices();

    Double getFuelPriceBlocking(String fuelType, String city);
}
