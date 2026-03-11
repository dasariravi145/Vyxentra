package com.vyxentra.vehicle.mapper;


import com.vyxentra.vehicle.dto.DamageItem;
import com.vyxentra.vehicle.dto.response.DamageItemResponse;
import com.vyxentra.vehicle.dto.response.DamageReportResponse;
import com.vyxentra.vehicle.entity.DamageReport;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface DamageMapper {

    @Mapping(target = "reportId", source = "id")
    @Mapping(target = "bookingId", source = "booking.id")
    @Mapping(target = "reportedByName", ignore = true)
    @Mapping(target = "images", expression = "java(mapImages(damageReport.getImages()))")
    @Mapping(target = "items", source = "items")
    DamageReportResponse toResponse(DamageReport damageReport);

    List<DamageReportResponse> toResponseList(List<DamageReport> damageReports);

    @Mapping(target = "itemId", source = "id")
    @Mapping(target = "images", expression = "java(mapImages(damageItem.getImages()))")
    DamageItemResponse toItemResponse(DamageItem damageItem);

    default List<String> mapImages(String[] images) {
        return images != null ? Arrays.asList(images) : List.of();
    }
}
