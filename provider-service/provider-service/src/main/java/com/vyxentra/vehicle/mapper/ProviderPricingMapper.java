package com.vyxentra.vehicle.mapper;


import com.vyxentra.vehicle.dto.response.ProviderPricingResponse;
import com.vyxentra.vehicle.entity.ProviderPricing;
import com.vyxentra.vehicle.enums.VehicleType;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface ProviderPricingMapper {

    @Mapping(target = "pricingId", source = "id")
    @Mapping(target = "providerId", source = "provider.id")
    @Mapping(target = "providerName", source = "provider.businessName")
    @Mapping(target = "serviceName", expression = "java(pricing.getServiceType() != null ? pricing.getServiceType().getDisplayName() : null)")
    @Mapping(target = "vehiclePricing", expression = "java(mapVehiclePricing(pricing.getVehiclePricing()))")
    ProviderPricingResponse toResponse(ProviderPricing pricing);

    List<ProviderPricingResponse> toResponseList(List<ProviderPricing> pricings);

    default Map<VehicleType, ProviderPricingResponse.VehiclePricing> mapVehiclePricing(
            Map<String, BigDecimal> vehiclePricing) {
        if (vehiclePricing == null) return Map.of();

        return vehiclePricing.entrySet().stream()
                .collect(Collectors.toMap(
                        e -> VehicleType.valueOf(e.getKey()),
                        e -> ProviderPricingResponse.VehiclePricing.builder()
                                .price(e.getValue())
                                .priceMultiplier(BigDecimal.ONE)
                                .isAvailable(true)
                                .build()
                ));
    }
}
