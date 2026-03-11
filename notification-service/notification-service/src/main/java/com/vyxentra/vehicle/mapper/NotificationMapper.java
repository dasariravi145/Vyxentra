package com.vyxentra.vehicle.mapper;

import com.vyxentra.vehicle.dto.response.NotificationHistoryResponse;
import com.vyxentra.vehicle.dto.response.NotificationPreferenceResponse;
import com.vyxentra.vehicle.entity.Notification;
import com.vyxentra.vehicle.entity.NotificationPreference;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface NotificationMapper {

    @Mapping(target = "notificationId", source = "id")
    NotificationHistoryResponse toHistoryResponse(Notification notification);

    List<NotificationHistoryResponse> toHistoryResponseList(List<Notification> notifications);

    NotificationPreferenceResponse toPreferenceResponse(NotificationPreference preference);
}
