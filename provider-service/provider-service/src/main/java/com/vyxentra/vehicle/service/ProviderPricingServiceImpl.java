package com.vyxentra.vehicle.service;


import com.vyxentra.vehicle.dto.request.ServicePricingRequest;
import com.vyxentra.vehicle.dto.response.ProviderPricingResponse;
import com.vyxentra.vehicle.entity.Provider;
import com.vyxentra.vehicle.entity.ProviderPricing;
import com.vyxentra.vehicle.enums.PricingAlgorithm;
import com.vyxentra.vehicle.enums.ServiceType;
import com.vyxentra.vehicle.enums.VehicleType;
import com.vyxentra.vehicle.exception.BusinessException;
import com.vyxentra.vehicle.exception.ErrorCode;
import com.vyxentra.vehicle.exception.ProviderNotFoundException;
import com.vyxentra.vehicle.kafka.ProviderEventProducer;
import com.vyxentra.vehicle.mapper.ProviderPricingMapper;
import com.vyxentra.vehicle.repository.ProviderPricingRepository;
import com.vyxentra.vehicle.repository.ProviderRepository;
import com.vyxentra.vehicle.service.ProviderPricingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProviderPricingServiceImpl implements ProviderPricingService {

    private final ProviderPricingRepository pricingRepository;
    private final ProviderRepository providerRepository;
    private final ProviderPricingMapper pricingMapper;
    private final ProviderEventProducer eventProducer;

    @Override
    @Transactional
    @CacheEvict(value = "providerPricing", key = "#providerId")
    public ProviderPricingResponse addOrUpdatePricing(String providerId, ServicePricingRequest request) {
        Provider provider = providerRepository.findById(providerId)
                .orElseThrow(() -> new ProviderNotFoundException(providerId));

        if (!provider.getStatus().isActive()) {
            throw new BusinessException(ErrorCode.PROVIDER_NOT_APPROVED);
        }

        if (request.getVehicleSpecificPricing() != null) {
            for (VehicleType vehicleType : request.getVehicleSpecificPricing().keySet()) {
                if (vehicleType == VehicleType.BIKE && !provider.getSupportsBike()) {
                    throw new BusinessException(ErrorCode.SERVICE_NOT_SUPPORTED,
                            "Provider does not support bike services");
                }
                if (vehicleType == VehicleType.CAR && !provider.getSupportsCar()) {
                    throw new BusinessException(ErrorCode.SERVICE_NOT_SUPPORTED,
                            "Provider does not support car services");
                }
            }
        }

        ProviderPricing pricing = pricingRepository
                .findByProviderIdAndServiceType(providerId, request.getServiceType())
                .orElse(new ProviderPricing());

        pricing.setProvider(provider);
        pricing.setServiceType(request.getServiceType());
        pricing.setBasePrice(request.getBasePrice());
        pricing.setCurrency(request.getCurrency() != null ? request.getCurrency() : "INR");
        pricing.setVehiclePricing(convertVehiclePricing(request.getVehicleSpecificPricing()));
        pricing.setEstimatedDurationMinutes(request.getEstimatedDurationMinutes());
        pricing.setDescription(request.getDescription());
        pricing.setIsActive(request.getIsActive() != null ? request.getIsActive() : true);
        pricing.setAlgorithm(request.getAlgorithm() != null ? request.getAlgorithm() : PricingAlgorithm.FIXED);
        pricing.setDynamicConfig(request.getDynamicConfig() != null ?
                convertDynamicConfig(request.getDynamicConfig()) : null);
        pricing.setTaxPercentage(request.getTaxPercentage());
        pricing.setAddons(request.getAddons() != null ? convertAddons(request.getAddons()) : null);
        pricing.setMetadata(request.getMetadata());

        pricing = pricingRepository.save(pricing);
        eventProducer.publishProviderUpdated(provider);

        return pricingMapper.toResponse(pricing);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "providerPricing", key = "#providerId")
    public List<ProviderPricingResponse> getProviderPricing(String providerId) {
        return pricingRepository.findByProviderId(providerId).stream()
                .map(pricingMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "servicePricing", key = "#providerId + '_' + #serviceType")
    public ProviderPricingResponse getServicePricing(String providerId, ServiceType serviceType) {
        ProviderPricing pricing = pricingRepository
                .findByProviderIdAndServiceType(providerId, serviceType)
                .orElseThrow(() -> new BusinessException(ErrorCode.PRICING_NOT_FOUND,
                        "Pricing not found for service: " + serviceType));
        return pricingMapper.toResponse(pricing);
    }

    @Override
    @Transactional
    @CacheEvict(value = {"providerPricing", "servicePricing"}, allEntries = true)
    public void updatePrice(String providerId, ServiceType serviceType, String vehicleType, BigDecimal price) {
        ProviderPricing pricing = pricingRepository
                .findByProviderIdAndServiceType(providerId, serviceType)
                .orElseThrow(() -> new BusinessException(ErrorCode.PRICING_NOT_FOUND));

        if (vehicleType == null) {
            pricing.setBasePrice(price);
        } else {
            Map<String, BigDecimal> vehiclePricing = pricing.getVehiclePricing();
            if (vehiclePricing == null) {
                vehiclePricing = new java.util.HashMap<>();
            }
            vehiclePricing.put(vehicleType, price);
            pricing.setVehiclePricing(vehiclePricing);
        }

        pricingRepository.save(pricing);
    }

    @Override
    @Transactional
    @CacheEvict(value = "providerPricing", key = "#providerId")
    public List<ProviderPricingResponse> bulkUpdatePricing(String providerId, List<ServicePricingRequest> requests) {
        return requests.stream()
                .map(request -> addOrUpdatePricing(providerId, request))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    @CacheEvict(value = {"providerPricing", "servicePricing"}, allEntries = true)
    public void deletePricing(String providerId, ServiceType serviceType) {
        ProviderPricing pricing = pricingRepository
                .findByProviderIdAndServiceType(providerId, serviceType)
                .orElseThrow(() -> new BusinessException(ErrorCode.PRICING_NOT_FOUND));

        pricing.setIsActive(false);
        pricingRepository.save(pricing);
    }

    private Map<String, BigDecimal> convertVehiclePricing(Map<VehicleType, BigDecimal> vehiclePricing) {
        if (vehiclePricing == null) return null;
        return vehiclePricing.entrySet().stream()
                .collect(Collectors.toMap(
                        e -> e.getKey().name(),
                        Map.Entry::getValue
                ));
    }

    private Map<String, Object> convertDynamicConfig(ServicePricingRequest.DynamicPricingConfig config) {
        return Map.of(
                "enabled", config.isEnabled(),
                "surgeMultiplier", config.getSurgeMultiplier(),
                "demandThreshold", config.getDemandThreshold(),
                "timeBasedMultiplier", config.getTimeBasedMultiplier(),
                "timeSlots", config.getTimeSlots()
        );
    }

    private Map<String, Object> convertAddons(List<ServicePricingRequest.AddonPricing> addons) {
        return Map.of("addons", addons);
    }
}
