package com.vyxentra.vehicle.controller;


import com.vyxentra.vehicle.dto.request.ServicePricingRequest;
import com.vyxentra.vehicle.dto.response.ApiResponse;
import com.vyxentra.vehicle.dto.response.ProviderPricingResponse;
import com.vyxentra.vehicle.enums.ServiceType;
import com.vyxentra.vehicle.service.ProviderPricingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/v1/providers/pricing")
@RequiredArgsConstructor
public class ProviderPricingController {

    private final ProviderPricingService pricingService;

    @PostMapping
    public ResponseEntity<ApiResponse<ProviderPricingResponse>> addOrUpdatePricing(
            @RequestHeader("X-User-ID") String userId,
            @Valid @RequestBody ServicePricingRequest request) {
        ProviderPricingResponse response = pricingService.addOrUpdatePricing(userId, request);
        return ResponseEntity.ok(ApiResponse.success(response, "Pricing saved successfully"));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<ProviderPricingResponse>>> getProviderPricing(
            @RequestHeader("X-User-ID") String userId) {
        List<ProviderPricingResponse> responses = pricingService.getProviderPricing(userId);
        return ResponseEntity.ok(ApiResponse.success(responses));
    }

    @GetMapping("/{serviceType}")
    public ResponseEntity<ApiResponse<ProviderPricingResponse>> getServicePricing(
            @RequestHeader("X-User-ID") String userId,
            @PathVariable ServiceType serviceType) {
        ProviderPricingResponse response = pricingService.getServicePricing(userId, serviceType);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PutMapping("/{serviceType}/price")
    public ResponseEntity<ApiResponse<Void>> updatePrice(
            @RequestHeader("X-User-ID") String userId,
            @PathVariable ServiceType serviceType,
            @RequestParam(required = false) String vehicleType,
            @RequestParam BigDecimal price) {
        pricingService.updatePrice(userId, serviceType, vehicleType, price);
        return ResponseEntity.ok(ApiResponse.success(null, "Price updated"));
    }

    @DeleteMapping("/{serviceType}")
    public ResponseEntity<ApiResponse<Void>> deletePricing(
            @RequestHeader("X-User-ID") String userId,
            @PathVariable ServiceType serviceType) {
        pricingService.deletePricing(userId, serviceType);
        return ResponseEntity.ok(ApiResponse.success(null, "Pricing deleted"));
    }
}
