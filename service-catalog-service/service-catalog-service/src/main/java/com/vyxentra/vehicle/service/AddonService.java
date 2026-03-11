package com.vyxentra.vehicle.service;



import com.vyxentra.vehicle.dto.request.AddonRequest;
import com.vyxentra.vehicle.dto.response.AddonResponse;

import java.util.List;

public interface AddonService {

    List<AddonResponse> getAddonsForService(String serviceId, Boolean active);

    AddonResponse getAddon(String addonId);

    AddonResponse createAddon(String adminId, String serviceId, AddonRequest request);

    AddonResponse updateAddon(String adminId, String addonId, AddonRequest request);

    void deleteAddon(String adminId, String addonId);

    void toggleAddonStatus(String adminId, String addonId, boolean active);

    void reorderAddons(String adminId, String serviceId, List<String> addonIds);
}
