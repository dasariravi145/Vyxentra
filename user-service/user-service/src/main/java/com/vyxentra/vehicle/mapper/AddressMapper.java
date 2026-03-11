package com.vyxentra.vehicle.mapper;

import com.vyxentra.vehicle.dto.request.AddAddressRequest;
import com.vyxentra.vehicle.dto.request.UpdateAddressRequest;
import com.vyxentra.vehicle.dto.response.AddressResponse;
import com.vyxentra.vehicle.entity.Address;
import com.vyxentra.vehicle.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface AddressMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "user", source = "user")
    @Mapping(target = "latitude", source = "request.latitude")
    @Mapping(target = "longitude", source = "request.longitude")
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Address toEntity(AddAddressRequest request, User user);

    @Mapping(target = "addressId", source = "id")
    AddressResponse toResponse(Address address);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "latitude", source = "latitude")
    @Mapping(target = "longitude", source = "longitude")
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateEntity(@MappingTarget Address address, UpdateAddressRequest request);
}