package com.vyxentra.vehicle.service;



import com.vyxentra.vehicle.dto.request.ProviderSearchRequest;
import com.vyxentra.vehicle.dto.response.ProviderSearchResponse;
import com.vyxentra.vehicle.entity.Provider;
import com.vyxentra.vehicle.mapper.ProviderMapper;
import com.vyxentra.vehicle.repository.ProviderRepository;
import com.vyxentra.vehicle.utils.GeoUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProviderSearchServiceImpl implements ProviderSearchService {

    private final ProviderRepository providerRepository;
    private final ProviderMapper providerMapper;
    private final GeoUtils geoUtils;

    @Override
    public List<ProviderSearchResponse> findNearbyProviders(Double latitude, Double longitude,
                                                            Integer radiusKm, String serviceType,
                                                            String vehicleType) {

        Boolean supportsBike = null;
        Boolean supportsCar = null;

        if (vehicleType != null) {
            if ("BIKE".equals(vehicleType)) {
                supportsBike = true;
            } else if ("CAR".equals(vehicleType)) {
                supportsCar = true;
            }
        }

        List<Provider> providers = providerRepository.findNearbyActiveProviders(
                latitude, longitude, radiusKm, supportsBike, supportsCar, null, 20);

        return providers.stream()
                .map(p -> {
                    double distance = geoUtils.calculateDistance(
                            latitude, longitude, p.getLatitude(), p.getLongitude());
                    return providerMapper.toSearchResponse(p, distance);
                })
                .collect(Collectors.toList());
    }

    @Override
    public List<ProviderSearchResponse> searchProviders(ProviderSearchRequest request) {
        // This would use specifications for complex search
        return List.of();
    }
}
