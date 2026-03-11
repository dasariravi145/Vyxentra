package com.vyxentra.vehicle.service;


import com.vyxentra.vehicle.dto.request.ProviderSearchRequest;
import com.vyxentra.vehicle.dto.response.ProviderSearchResponse;

import java.util.List;

public interface ProviderSearchService {

    List<ProviderSearchResponse> findNearbyProviders(Double latitude, Double longitude,
                                                     Integer radiusKm, String serviceType,
                                                     String vehicleType);

    List<ProviderSearchResponse> searchProviders(ProviderSearchRequest request);
}