package com.vyxentra.vehicle.mapper;


import com.vyxentra.vehicle.dto.response.CategoryResponse;
import com.vyxentra.vehicle.entity.ServiceCategory;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface CategoryMapper {

    @Mapping(target = "categoryId", source = "id")
    @Mapping(target = "serviceCount", ignore = true)
    CategoryResponse toResponse(ServiceCategory category);

    List<CategoryResponse> toResponseList(List<ServiceCategory> categories);

    default CategoryResponse toResponseWithCount(ServiceCategory category, Integer serviceCount) {
        CategoryResponse response = toResponse(category);
        response.setServiceCount(serviceCount);
        return response;
    }
}
