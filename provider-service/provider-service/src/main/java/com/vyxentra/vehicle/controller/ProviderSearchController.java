package com.vyxentra.vehicle.controller;


import com.vyxentra.vehicle.dto.request.ProviderSearchRequest;
import com.vyxentra.vehicle.dto.response.ApiResponse;
import com.vyxentra.vehicle.dto.response.ProviderSearchResponse;
import com.vyxentra.vehicle.service.ProviderSearchService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/v1/providers/search")
@RequiredArgsConstructor
public class ProviderSearchController {

    private final ProviderSearchService searchService;

    @GetMapping("/nearby")
    public ResponseEntity<ApiResponse<List<ProviderSearchResponse>>> findNearbyProviders(
            @RequestParam Double latitude,
            @RequestParam Double longitude,
            @RequestParam(required = false, defaultValue = "5") Integer radiusKm,
            @RequestParam(required = false) String serviceType,
            @RequestParam(required = false) String vehicleType) {

        List<ProviderSearchResponse> providers = searchService.findNearbyProviders(
                latitude, longitude, radiusKm, serviceType, vehicleType);
        return ResponseEntity.ok(ApiResponse.success(providers));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<List<ProviderSearchResponse>>> searchProviders(
            @Valid @RequestBody ProviderSearchRequest request) {
        List<ProviderSearchResponse> providers = searchService.searchProviders(request);
        return ResponseEntity.ok(ApiResponse.success(providers));
    }
}
