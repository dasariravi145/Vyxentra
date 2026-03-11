package com.vyxentra.vehicle.service;


import com.vyxentra.vehicle.dto.ServiceVehiclePricing;
import com.vyxentra.vehicle.dto.request.SearchServicesRequest;
import com.vyxentra.vehicle.dto.request.ServiceDefinitionRequest;
import com.vyxentra.vehicle.dto.response.PageResponse;
import com.vyxentra.vehicle.dto.response.ServiceDefinitionResponse;
import com.vyxentra.vehicle.dto.response.ServiceDetailResponse;
import com.vyxentra.vehicle.dto.response.ServiceSearchResponse;
import com.vyxentra.vehicle.entity.*;
import com.vyxentra.vehicle.enums.ProviderType;
import com.vyxentra.vehicle.enums.ServiceType;
import com.vyxentra.vehicle.enums.VehicleType;
import com.vyxentra.vehicle.exceptions.BusinessException;
import com.vyxentra.vehicle.exceptions.ErrorCode;
import com.vyxentra.vehicle.exceptions.ResourceNotFoundException;
import com.vyxentra.vehicle.mapper.AddonMapper;
import com.vyxentra.vehicle.mapper.ServiceDefinitionMapper;
import com.vyxentra.vehicle.repository.ServiceAddonRepository;
import com.vyxentra.vehicle.repository.ServiceCategoryRepository;
import com.vyxentra.vehicle.repository.ServiceDefinitionRepository;
import com.vyxentra.vehicle.repository.ServicePricingRuleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ServiceCatalogServiceImpl implements ServiceCatalogService {

    private final ServiceDefinitionRepository serviceRepository;
    private final ServiceCategoryRepository categoryRepository;
    private final ServiceAddonRepository addonRepository;
    private final ServicePricingRuleRepository pricingRuleRepository;
    private final ServiceDefinitionMapper serviceMapper;
    private final AddonMapper addonMapper;
    private final ServiceCacheManager cacheManager;

    @Override
    @Cacheable(value = "allServices", key = "#active", unless = "#result == null")
    public List<ServiceDefinitionResponse> getAllServices(Boolean active) {
        log.debug("Getting all services, active: {}", active);

        List<ServiceDefinition> services;
        if (Boolean.TRUE.equals(active)) {
            services = serviceRepository.findByIsActiveTrue();
        } else {
            services = serviceRepository.findAll();
        }

        return serviceMapper.toResponseList(services);
    }

    @Override
    @Cacheable(value = "servicesPaginated", key = "#pageable.pageNumber + '-' + #pageable.pageSize + '-' + " +
            "#category + '-' + #vehicleType + '-' + #providerType + '-' + #active")
    public PageResponse<ServiceDefinitionResponse> getServicesPaginated(Pageable pageable, String category,
                                                                        String vehicleType, String providerType,
                                                                        Boolean active) {
        log.debug("Getting paginated services with filters");

        ProviderType providerTypeEnum = providerType != null ? ProviderType.valueOf(providerType) : null;

        Page<ServiceDefinition> page = serviceRepository.findByFilters(
                category, vehicleType, providerTypeEnum, active, pageable);

        return PageResponse.<ServiceDefinitionResponse>builder()
                .content(serviceMapper.toResponseList(page.getContent()))
                .pageNumber(page.getNumber())
                .pageSize(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .first(page.isFirst())
                .last(page.isLast())
                .hasNext(page.hasNext())
                .hasPrevious(page.hasPrevious())
                .build();
    }

    @Override
    @Cacheable(value = "serviceDetails", key = "#serviceId", unless = "#result == null")
    public ServiceDetailResponse getServiceDetails(String serviceId) {
        log.debug("Getting service details: {}", serviceId);

        ServiceDefinition service = findServiceById(serviceId);
        ServiceDetailResponse response = serviceMapper.toDetailResponse(service);

        // Add addons
        response.setAddons(addonMapper.toResponseList(
                addonRepository.findByServiceIdAndIsActiveTrueOrderByDisplayOrderAsc(serviceId)));

        // Add FAQs
        // This would be fetched from FAQ repository

        return response;
    }

    @Override
    @Cacheable(value = "serviceByType", key = "#serviceType", unless = "#result == null")
    public ServiceDetailResponse getServiceByType(String serviceType) {
        log.debug("Getting service by type: {}", serviceType);

        ServiceType type = ServiceType.valueOf(serviceType);
        ServiceDefinition service = serviceRepository.findByServiceType(type)
                .orElseThrow(() -> new ResourceNotFoundException("Service", "type", serviceType));

        return getServiceDetails(service.getId());
    }

    @Override
    public List<ServiceSearchResponse> searchServices(SearchServicesRequest request) {
        log.debug("Searching services with query: {}", request.getQuery());

        List<ServiceDefinition> services = serviceRepository.searchByQuery(request.getQuery());

        // Apply filters
        if (request.getCategory() != null) {
            services = services.stream()
                    .filter(s -> s.getCategory().getId().equals(request.getCategory()))
                    .collect(Collectors.toList());
        }

        if (request.getVehicleType() != null) {
            VehicleType vehicleType = VehicleType.valueOf(request.getVehicleType());
            services = services.stream()
                    .filter(s -> s.getVehicleTypes().stream()
                            .anyMatch(vt -> vt.getVehicleType() == vehicleType && vt.getIsActive()))
                    .collect(Collectors.toList());
        }

        if (request.getMinPrice() != null) {
            services = services.stream()
                    .filter(s -> s.getMinPrice() != null && s.getMinPrice() >= request.getMinPrice())
                    .collect(Collectors.toList());
        }

        if (request.getMaxPrice() != null) {
            services = services.stream()
                    .filter(s -> s.getMaxPrice() != null && s.getMaxPrice() <= request.getMaxPrice())
                    .collect(Collectors.toList());
        }

        // Convert to search response
        List<ServiceSearchResponse> responses = new ArrayList<>();
        for (ServiceDefinition service : services) {
            // Calculate relevance score based on name match priority
            double relevance = 0.0;
            if (service.getName().toLowerCase().contains(request.getQuery().toLowerCase())) {
                relevance += 10.0;
            }
            if (service.getDescription() != null &&
                    service.getDescription().toLowerCase().contains(request.getQuery().toLowerCase())) {
                relevance += 5.0;
            }

            // Get base price for default vehicle
            Double price = service.getMinPrice();

            responses.add(ServiceSearchResponse.builder()
                    .serviceId(service.getId())
                    .name(service.getName())
                    .shortDescription(service.getShortDescription())
                    .categoryName(service.getCategory().getName())
                    .icon(service.getIcon())
                    .price(price)
                    .isPopular(service.getIsPopular())
                    .isRecommended(service.getIsRecommended())
                    .estimatedDurationMin(service.getEstimatedDurationMin())
                    .relevanceScore(relevance)
                    .build());
        }

        // Sort by relevance
        responses.sort((a, b) -> Double.compare(b.getRelevanceScore(), a.getRelevanceScore()));

        // Apply limit
        if (request.getLimit() != null && responses.size() > request.getLimit()) {
            responses = responses.subList(0, request.getLimit());
        }

        return responses;
    }

    @Override
    @Cacheable(value = "popularServices", key = "#vehicleType + '-' + #limit")
    public List<ServiceDefinitionResponse> getPopularServices(String vehicleType, Integer limit) {
        log.debug("Getting popular services for vehicle: {}", vehicleType);

        List<ServiceDefinition> services;

        if (vehicleType != null) {
            services = serviceRepository.findByVehicleType(vehicleType).stream()
                    .filter(ServiceDefinition::getIsPopular)
                    .limit(limit)
                    .collect(Collectors.toList());
        } else {
            services = serviceRepository.findPopularServices().stream()
                    .limit(limit)
                    .collect(Collectors.toList());
        }

        return serviceMapper.toResponseList(services);
    }

    @Override
    @Cacheable(value = "recommendedServices", key = "#vehicleType")
    public List<ServiceDefinitionResponse> getRecommendedServices(String vehicleType) {
        log.debug("Getting recommended services for vehicle: {}", vehicleType);

        List<ServiceDefinition> services;

        if (vehicleType != null) {
            services = serviceRepository.findByVehicleType(vehicleType).stream()
                    .filter(ServiceDefinition::getIsRecommended)
                    .collect(Collectors.toList());
        } else {
            services = serviceRepository.findRecommendedServices();
        }

        return serviceMapper.toResponseList(services);
    }

    @Override
    @Cacheable(value = "servicesByVehicle", key = "#vehicleType + '-' + #providerType")
    public List<ServiceDefinitionResponse> getServicesByVehicleType(String vehicleType, String providerType) {
        log.debug("Getting services for vehicle: {}, provider: {}", vehicleType, providerType);

        List<ServiceDefinition> services = serviceRepository.findByVehicleType(vehicleType);

        if (providerType != null) {
            ProviderType pt = ProviderType.valueOf(providerType);
            services = services.stream()
                    .filter(s -> s.getProviderType() == pt)
                    .collect(Collectors.toList());
        }

        return serviceMapper.toResponseList(services);
    }

    @Override
    @Transactional
    @CacheEvict(value = {"allServices", "servicesPaginated", "serviceDetails", "serviceByType",
            "popularServices", "recommendedServices", "servicesByVehicle"}, allEntries = true)
    public ServiceDefinitionResponse createService(String adminId, ServiceDefinitionRequest request) {
        log.info("Creating new service by admin: {}", adminId);

        // Validate service type uniqueness
        if (serviceRepository.existsByServiceType(request.getServiceType())) {
            throw new BusinessException(ErrorCode.VALIDATION_ERROR,
                    "Service type already exists: " + request.getServiceType());
        }

        // Get category
        ServiceCategory category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Category", request.getCategoryId()));

        // Create service
        ServiceDefinition service = ServiceDefinition.builder()
                .category(category)
                .serviceType(request.getServiceType())
                .name(request.getName())
                .description(request.getDescription())
                .shortDescription(request.getShortDescription())
                .icon(request.getIcon())
                .imageUrl(request.getImageUrl())
                .estimatedDurationMin(request.getEstimatedDurationMin())
                .providerType(request.getProviderType())
                .isActive(request.getIsActive() != null ? request.getIsActive() : true)
                .isPopular(request.getIsPopular() != null ? request.getIsPopular() : false)
                .isRecommended(request.getIsRecommended() != null ? request.getIsRecommended() : false)
                .displayOrder(request.getDisplayOrder())
                .tags(request.getTags() != null ? String.join(",", request.getTags()) : null)
                .createdBy(adminId)
                .build();

        service = serviceRepository.save(service);

        // Add vehicle type pricing
        double minPrice = Double.MAX_VALUE;
        double maxPrice = 0.0;

        for (var entry : request.getVehiclePricing().entrySet()) {
            VehicleType vehicleType = VehicleType.valueOf(entry.getKey());
            ServiceVehiclePricing pricing = entry.getValue();

            ServiceVehicleType vehicleTypeEntity = ServiceVehicleType.builder()
                    .service(service)
                    .vehicleType(vehicleType)
                    .basePrice(pricing.getBasePrice())
                    .priceMultiplier(pricing.getPriceMultiplier() != null ? pricing.getPriceMultiplier() : 1.0)
                    .estimatedDurationMin(pricing.getEstimatedDurationMin())
                    .isActive(pricing.getIsActive() != null ? pricing.getIsActive() : true)
                    .build();

            service.getVehicleTypes().add(vehicleTypeEntity);

            // Update min/max prices
            minPrice = Math.min(minPrice, pricing.getBasePrice());
            maxPrice = Math.max(maxPrice, pricing.getBasePrice());
        }

        service.setMinPrice(minPrice);
        service.setMaxPrice(maxPrice);

        // Add addons
        if (request.getAddons() != null) {
            for (var addonRequest : request.getAddons()) {
                ServiceAddon addon = ServiceAddon.builder()
                        .service(service)
                        .name(addonRequest.getName())
                        .description(addonRequest.getDescription())
                        .priceType(addonRequest.getPriceType())
                        .basePrice(addonRequest.getBasePrice())
                        .isMandatory(addonRequest.getIsMandatory() != null ? addonRequest.getIsMandatory() : false)
                        .isActive(addonRequest.getIsActive() != null ? addonRequest.getIsActive() : true)
                        .displayOrder(addonRequest.getDisplayOrder())
                        .build();

                service.getAddons().add(addon);

                // Add vehicle-specific pricing for addon
                if (addonRequest.getVehiclePricing() != null) {
                    for (var vehicleEntry : addonRequest.getVehiclePricing().entrySet()) {
                        AddonVehiclePricing vehiclePricing = AddonVehiclePricing.builder()
                                .addon(addon)
                                .vehicleType(VehicleType.valueOf(vehicleEntry.getKey()))
                                .price(vehicleEntry.getValue())
                                .build();
                        addon.getVehiclePricing().add(vehiclePricing);
                    }
                }
            }
        }

        service = serviceRepository.save(service);

        // Clear cache
        cacheManager.evictAllServiceCaches();

        log.info("Service created successfully with ID: {}", service.getId());

        return serviceMapper.toResponse(service);
    }

    @Override
    @Transactional
    @CacheEvict(value = {"allServices", "servicesPaginated", "serviceDetails", "serviceByType",
            "popularServices", "recommendedServices", "servicesByVehicle"}, allEntries = true)
    public ServiceDefinitionResponse updateService(String adminId, String serviceId,
                                                   ServiceDefinitionRequest request) {
        log.info("Updating service: {} by admin: {}", serviceId, adminId);

        ServiceDefinition service = findServiceById(serviceId);

        // Update basic info
        service.setName(request.getName());
        service.setDescription(request.getDescription());
        service.setShortDescription(request.getShortDescription());
        service.setIcon(request.getIcon());
        service.setImageUrl(request.getImageUrl());
        service.setEstimatedDurationMin(request.getEstimatedDurationMin());
        service.setIsPopular(request.getIsPopular());
        service.setIsRecommended(request.getIsRecommended());
        service.setDisplayOrder(request.getDisplayOrder());
        service.setTags(request.getTags() != null ? String.join(",", request.getTags()) : null);
        service.setUpdatedBy(adminId);

        // Update vehicle pricing
        service.getVehicleTypes().clear();

        double minPrice = Double.MAX_VALUE;
        double maxPrice = 0.0;

        for (var entry : request.getVehiclePricing().entrySet()) {
            VehicleType vehicleType = VehicleType.valueOf(entry.getKey());
            ServiceVehiclePricing pricing = entry.getValue();

            ServiceVehicleType vehicleTypeEntity = ServiceVehicleType.builder()
                    .service(service)
                    .vehicleType(vehicleType)
                    .basePrice(pricing.getBasePrice())
                    .priceMultiplier(pricing.getPriceMultiplier() != null ? pricing.getPriceMultiplier() : 1.0)
                    .estimatedDurationMin(pricing.getEstimatedDurationMin())
                    .isActive(pricing.getIsActive() != null ? pricing.getIsActive() : true)
                    .build();

            service.getVehicleTypes().add(vehicleTypeEntity);

            minPrice = Math.min(minPrice, pricing.getBasePrice());
            maxPrice = Math.max(maxPrice, pricing.getBasePrice());
        }

        service.setMinPrice(minPrice);
        service.setMaxPrice(maxPrice);

        service = serviceRepository.save(service);

        log.info("Service updated successfully: {}", serviceId);

        return serviceMapper.toResponse(service);
    }

    @Override
    @Transactional
    @CacheEvict(value = {"allServices", "servicesPaginated", "serviceDetails", "serviceByType",
            "popularServices", "recommendedServices", "servicesByVehicle"}, allEntries = true)
    public void deleteService(String adminId, String serviceId) {
        log.info("Deleting service: {} by admin: {}", serviceId, adminId);

        ServiceDefinition service = findServiceById(serviceId);

        // Soft delete by deactivating
        service.setIsActive(false);
        service.setUpdatedBy(adminId);
        serviceRepository.save(service);

        log.info("Service deactivated: {}", serviceId);
    }

    @Override
    @Transactional
    @CacheEvict(value = {"allServices", "servicesPaginated", "serviceDetails", "serviceByType",
            "popularServices", "recommendedServices", "servicesByVehicle"}, allEntries = true)
    public void toggleServiceStatus(String adminId, String serviceId, boolean active) {
        log.info("Toggling service: {} to {} by admin: {}", serviceId, active, adminId);

        ServiceDefinition service = findServiceById(serviceId);
        service.setIsActive(active);
        service.setUpdatedBy(adminId);
        serviceRepository.save(service);

        log.info("Service status toggled: {} -> {}", serviceId, active);
    }

    @Override
    @Transactional
    @CacheEvict(value = {"serviceDetails", "serviceByType"}, key = "#serviceId", allEntries = true)
    public void updateServicePricing(String adminId, String serviceId, String vehicleType, Double basePrice) {
        log.info("Updating pricing for service: {}, vehicle: {}, price: {}", serviceId, vehicleType, basePrice);

        ServiceDefinition service = findServiceById(serviceId);

        service.getVehicleTypes().stream()
                .filter(vt -> vt.getVehicleType().name().equals(vehicleType))
                .findFirst()
                .ifPresent(vt -> {
                    vt.setBasePrice(basePrice);

                    // Update min/max prices
                    double minPrice = service.getVehicleTypes().stream()
                            .mapToDouble(ServiceVehicleType::getBasePrice)
                            .min().orElse(0.0);
                    double maxPrice = service.getVehicleTypes().stream()
                            .mapToDouble(ServiceVehicleType::getBasePrice)
                            .max().orElse(0.0);

                    service.setMinPrice(minPrice);
                    service.setMaxPrice(maxPrice);
                });

        service.setUpdatedBy(adminId);
        serviceRepository.save(service);

        log.info("Pricing updated successfully");
    }

    @Override
    public List<ServiceDefinitionResponse> getServicesPendingReview() {
        // This would return services that need admin review
        // For now, return services created in last 24 hours
        return List.of();
    }

    private ServiceDefinition findServiceById(String serviceId) {
        return serviceRepository.findById(serviceId)
                .orElseThrow(() -> new ResourceNotFoundException("Service", serviceId));
    }
}
