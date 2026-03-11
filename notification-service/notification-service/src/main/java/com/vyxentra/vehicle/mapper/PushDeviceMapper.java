package com.vyxentra.vehicle.mapper;


import com.vyxentra.vehicle.dto.response.PushDeviceResponse;
import com.vyxentra.vehicle.entity.PushDevice;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface PushDeviceMapper {

    @Mapping(target = "deviceId", source = "id")
    PushDeviceResponse toResponse(PushDevice pushDevice);

    List<PushDeviceResponse> toResponseList(List<PushDevice> pushDevices);
}