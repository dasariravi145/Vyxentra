package com.vyxentra.vehicle.mapper;

import com.vyxentra.vehicle.dto.response.AddonResponse;
import com.vyxentra.vehicle.entity.AddonVehiclePricing;
import com.vyxentra.vehicle.entity.ServiceAddon;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.ReportingPolicy;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface AddonMapper {

    @Mapping(target = "addonId", source = "id")
    @Mapping(target = "serviceId", source = "service.id")
    @Mapping(target = "vehiclePricing", source = "vehiclePricing", qualifiedByName = "pricingToMap")
    AddonResponse toResponse(ServiceAddon addon);

    List<AddonResponse> toResponseList(List<ServiceAddon> addons);

    @Named("pricingToMap")
    default Map<String, Double> pricingToMap(List<AddonVehiclePricing> pricing) {
        return pricing.stream()
                .collect(Collectors.toMap(
                        p -> p.getVehicleType().name(),
                        AddonVehiclePricing::getPrice
                ));
    }
}
