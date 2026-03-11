package com.vyxentra.vehicle.service;

import com.vyxentra.vehicle.dto.request.AddonRequest;
import com.vyxentra.vehicle.dto.response.AddonResponse;
import com.vyxentra.vehicle.entity.AddonVehiclePricing;
import com.vyxentra.vehicle.entity.ServiceAddon;
import com.vyxentra.vehicle.entity.ServiceDefinition;
import com.vyxentra.vehicle.enums.VehicleType;
import com.vyxentra.vehicle.exceptions.ResourceNotFoundException;
import com.vyxentra.vehicle.mapper.AddonMapper;
import com.vyxentra.vehicle.repository.ServiceAddonRepository;
import com.vyxentra.vehicle.repository.ServiceDefinitionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class AddonServiceImpl implements AddonService {

    private final ServiceAddonRepository addonRepository;
    private final ServiceDefinitionRepository serviceRepository;
    private final AddonMapper addonMapper;
    private final ServiceCacheManager cacheManager;

    @Override
    @Cacheable(value = "addonsForService", key = "#serviceId + '-' + #active")
    public List<AddonResponse> getAddonsForService(String serviceId, Boolean active) {
        log.debug("Getting addons for service: {}, active: {}", serviceId, active);

        List<ServiceAddon> addons;
        if (Boolean.TRUE.equals(active)) {
            addons = addonRepository.findByServiceIdAndIsActiveTrueOrderByDisplayOrderAsc(serviceId);
        } else {
            addons = addonRepository.findByServiceIdOrderByDisplayOrderAsc(serviceId);
        }

        return addonMapper.toResponseList(addons);
    }

    @Override
    @Cacheable(value = "addon", key = "#addonId", unless = "#result == null")
    public AddonResponse getAddon(String addonId) {
        log.debug("Getting addon: {}", addonId);

        ServiceAddon addon = findAddonById(addonId);
        return addonMapper.toResponse(addon);
    }

    @Override
    @Transactional
    @CacheEvict(value = {"addonsForService", "addon", "serviceDetails"}, allEntries = true)
    public AddonResponse createAddon(String adminId, String serviceId, AddonRequest request) {
        log.info("Creating addon for service: {} by admin: {}", serviceId, adminId);

        ServiceDefinition service = serviceRepository.findById(serviceId)
                .orElseThrow(() -> new ResourceNotFoundException("Service", serviceId));

        ServiceAddon addon = ServiceAddon.builder()
                .service(service)
                .name(request.getName())
                .description(request.getDescription())
                .priceType(request.getPriceType())
                .basePrice(request.getBasePrice())
                .isMandatory(request.getIsMandatory() != null ? request.getIsMandatory() : false)
                .isActive(request.getIsActive() != null ? request.getIsActive() : true)
                .displayOrder(request.getDisplayOrder())
                .build();

        // Add vehicle-specific pricing
        if (request.getVehiclePricing() != null) {
            for (var entry : request.getVehiclePricing().entrySet()) {
                AddonVehiclePricing pricing = AddonVehiclePricing.builder()
                        .addon(addon)
                        .vehicleType(VehicleType.valueOf(entry.getKey()))
                        .price(entry.getValue())
                        .build();
                addon.getVehiclePricing().add(pricing);
            }
        }

        addon = addonRepository.save(addon);

        log.info("Addon created successfully with ID: {}", addon.getId());

        return addonMapper.toResponse(addon);
    }

    @Override
    @Transactional
    @CacheEvict(value = {"addonsForService", "addon", "serviceDetails"}, allEntries = true)
    public AddonResponse updateAddon(String adminId, String addonId, AddonRequest request) {
        log.info("Updating addon: {} by admin: {}", addonId, adminId);

        ServiceAddon addon = findAddonById(addonId);

        addon.setName(request.getName());
        addon.setDescription(request.getDescription());
        addon.setPriceType(request.getPriceType());
        addon.setBasePrice(request.getBasePrice());
        addon.setIsMandatory(request.getIsMandatory() != null ? request.getIsMandatory() : addon.getIsMandatory());
        addon.setIsActive(request.getIsActive() != null ? request.getIsActive() : addon.getIsActive());
        addon.setDisplayOrder(request.getDisplayOrder());

        // Update vehicle pricing
        addon.getVehiclePricing().clear();

        if (request.getVehiclePricing() != null) {
            for (var entry : request.getVehiclePricing().entrySet()) {
                AddonVehiclePricing pricing = AddonVehiclePricing.builder()
                        .addon(addon)
                        .vehicleType(VehicleType.valueOf(entry.getKey()))
                        .price(entry.getValue())
                        .build();
                addon.getVehiclePricing().add(pricing);
            }
        }

        addon = addonRepository.save(addon);

        log.info("Addon updated successfully: {}", addonId);

        return addonMapper.toResponse(addon);
    }

    @Override
    @Transactional
    @CacheEvict(value = {"addonsForService", "addon", "serviceDetails"}, allEntries = true)
    public void deleteAddon(String adminId, String addonId) {
        log.info("Deleting addon: {} by admin: {}", addonId, adminId);

        ServiceAddon addon = findAddonById(addonId);

        // Soft delete by deactivating
        addon.setIsActive(false);
        addonRepository.save(addon);

        log.info("Addon deactivated: {}", addonId);
    }

    @Override
    @Transactional
    @CacheEvict(value = {"addonsForService", "addon", "serviceDetails"}, allEntries = true)
    public void toggleAddonStatus(String adminId, String addonId, boolean active) {
        log.info("Toggling addon: {} to {} by admin: {}", addonId, active, adminId);

        ServiceAddon addon = findAddonById(addonId);
        addon.setIsActive(active);
        addonRepository.save(addon);

        log.info("Addon status toggled: {} -> {}", addonId, active);
    }

    @Override
    @Transactional
    @CacheEvict(value = {"addonsForService", "addon", "serviceDetails"}, allEntries = true)
    public void reorderAddons(String adminId, String serviceId, List<String> addonIds) {
        log.info("Reordering addons for service: {} by admin: {}", serviceId, adminId);

        for (int i = 0; i < addonIds.size(); i++) {
            String addonId = addonIds.get(i);
            addonRepository.updateDisplayOrder(addonId, i);
        }

        log.info("Addons reordered successfully");
    }

    private ServiceAddon findAddonById(String addonId) {
        return addonRepository.findById(addonId)
                .orElseThrow(() -> new ResourceNotFoundException("Addon", addonId));
    }
}
