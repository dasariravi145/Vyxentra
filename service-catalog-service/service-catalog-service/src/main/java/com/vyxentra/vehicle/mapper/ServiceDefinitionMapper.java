package com.vyxentra.vehicle.mapper;


import com.vyxentra.vehicle.dto.response.ServiceDefinitionResponse;
import com.vyxentra.vehicle.dto.response.ServiceDetailResponse;
import com.vyxentra.vehicle.entity.ServiceDefinition;
import com.vyxentra.vehicle.entity.ServiceVehicleType;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.ReportingPolicy;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE,
        uses = {CategoryMapper.class, AddonMapper.class})
public interface ServiceDefinitionMapper {

    @Mapping(target = "serviceId", source = "id")
    @Mapping(target = "categoryId", source = "category.id")
    @Mapping(target = "categoryName", source = "category.name")
    @Mapping(target = "tags", source = "tags", qualifiedByName = "stringToList")
    ServiceDefinitionResponse toResponse(ServiceDefinition service);

    List<ServiceDefinitionResponse> toResponseList(List<ServiceDefinition> services);

    @Mapping(target = "serviceId", source = "id")
    @Mapping(target = "categoryId", source = "category.id")
    @Mapping(target = "categoryName", source = "category.name")
    @Mapping(target = "tags", source = "tags", qualifiedByName = "stringToList")
    @Mapping(target = "vehicleDetails", source = "vehicleTypes", qualifiedByName = "vehicleTypesToMap")
    ServiceDetailResponse toDetailResponse(ServiceDefinition service);

    @Named("stringToList")
    default List<String> stringToList(String tags) {
        if (tags == null || tags.isEmpty()) {
            return List.of();
        }
        return Arrays.asList(tags.split(","));
    }

    @Named("vehicleTypesToMap")
    default Map<String, ServiceDetailResponse.ServiceVehicleDetail> vehicleTypesToMap(
            List<ServiceVehicleType> vehicleTypes) {
        return vehicleTypes.stream()
                .filter(ServiceVehicleType::getIsActive)
                .collect(Collectors.toMap(
                        vt -> vt.getVehicleType().name(),
                        vt -> ServiceDetailResponse.ServiceVehicleDetail.builder()
                                .basePrice(vt.getBasePrice())
                                .priceMultiplier(vt.getPriceMultiplier())
                                .estimatedDurationMin(vt.getEstimatedDurationMin())
                                .isActive(vt.getIsActive())
                                .build()
                ));
    }
}
