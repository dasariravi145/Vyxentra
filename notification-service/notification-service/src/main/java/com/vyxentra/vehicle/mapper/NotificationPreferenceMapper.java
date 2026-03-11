package com.vyxentra.vehicle.mapper;

import com.vyxentra.vehicle.dto.response.NotificationPreferenceResponse;
import com.vyxentra.vehicle.entity.NotificationPreference;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface NotificationPreferenceMapper {

    @Mapping(target = "userId", source = "userId")
    NotificationPreferenceResponse toResponse(NotificationPreference preference);
}
