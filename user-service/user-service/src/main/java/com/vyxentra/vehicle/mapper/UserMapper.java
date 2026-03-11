package com.vyxentra.vehicle.mapper;


import com.vyxentra.vehicle.dto.response.AddressResponse;
import com.vyxentra.vehicle.dto.response.UserProfileResponse;
import com.vyxentra.vehicle.dto.response.VehicleResponse;
import com.vyxentra.vehicle.entity.Address;
import com.vyxentra.vehicle.entity.User;
import com.vyxentra.vehicle.entity.Vehicle;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface UserMapper {

    UserProfileResponse toProfileResponse(User user);

    @Mapping(target = "addressId", source = "id")
    AddressResponse toAddressResponse(Address address);

    @Mapping(target = "vehicleId", source = "id")
    VehicleResponse toVehicleResponse(Vehicle vehicle);

    List<AddressResponse> toAddressResponseList(List<Address> addresses);

    List<VehicleResponse> toVehicleResponseList(List<Vehicle> vehicles);

    default UserProfileResponse toDetailedProfileResponse(User user,
                                                          List<AddressResponse> addresses,
                                                          List<VehicleResponse> vehicles,
                                                          AddressResponse defaultAddress,
                                                          VehicleResponse defaultVehicle) {
        UserProfileResponse response = toProfileResponse(user);
        response.setAddresses(addresses);
        response.setVehicles(vehicles);
        response.setDefaultAddress(defaultAddress);
        response.setDefaultVehicle(defaultVehicle);
        return response;
    }
}