package com.vyxentra.vehicle.mapper;


import com.vyxentra.vehicle.dto.request.AddVehicleRequest;
import com.vyxentra.vehicle.dto.request.UpdateVehicleRequest;
import com.vyxentra.vehicle.dto.response.VehicleResponse;
import com.vyxentra.vehicle.entity.User;
import com.vyxentra.vehicle.entity.Vehicle;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface VehicleMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "user", source = "user")
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Vehicle toEntity(AddVehicleRequest request, User user);

    @Mapping(target = "vehicleId", source = "id")
    VehicleResponse toResponse(Vehicle vehicle);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "registrationNumber", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateEntity(@MappingTarget Vehicle vehicle, UpdateVehicleRequest request);
}