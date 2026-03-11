package com.vyxentra.vehicle.service;



import com.vyxentra.vehicle.dto.request.AddVehicleRequest;
import com.vyxentra.vehicle.dto.request.UpdateVehicleRequest;
import com.vyxentra.vehicle.dto.response.PageResponse;
import com.vyxentra.vehicle.dto.response.VehicleResponse;
import org.springframework.data.domain.Pageable;

public interface VehicleService {

    VehicleResponse addVehicle(String userId, AddVehicleRequest request);

    PageResponse<VehicleResponse> getUserVehicles(String userId, Pageable pageable);

    VehicleResponse getVehicle(String userId, String vehicleId);

    VehicleResponse updateVehicle(String userId, String vehicleId, UpdateVehicleRequest request);

    void deleteVehicle(String userId, String vehicleId);

    void setDefaultVehicle(String userId, String vehicleId);

    boolean checkRegistrationNumberExists(String registrationNumber);
}
