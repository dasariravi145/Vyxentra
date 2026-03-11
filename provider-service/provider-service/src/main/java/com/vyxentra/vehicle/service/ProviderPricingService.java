package com.vyxentra.vehicle.service;


import com.vyxentra.vehicle.dto.request.ServicePricingRequest;
import com.vyxentra.vehicle.dto.response.ProviderPricingResponse;
import com.vyxentra.vehicle.enums.ServiceType;

import java.math.BigDecimal;
import java.util.List;

public interface ProviderPricingService {

    ProviderPricingResponse addOrUpdatePricing(String providerId, ServicePricingRequest request);

    List<ProviderPricingResponse> getProviderPricing(String providerId);

    ProviderPricingResponse getServicePricing(String providerId, ServiceType serviceType);

    void updatePrice(String providerId, ServiceType serviceType, String vehicleType, BigDecimal price);

    List<ProviderPricingResponse> bulkUpdatePricing(String providerId, List<ServicePricingRequest> requests);

    void deletePricing(String providerId, ServiceType serviceType);
}
