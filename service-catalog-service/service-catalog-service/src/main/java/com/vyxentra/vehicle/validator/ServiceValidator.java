package com.vyxentra.vehicle.validator;


import com.vyxentra.vehicle.enums.ProviderType;
import com.vyxentra.vehicle.enums.ServiceType;
import com.vyxentra.vehicle.enums.VehicleType;
import com.vyxentra.vehicle.exceptions.BusinessException;
import com.vyxentra.vehicle.exceptions.ErrorCode;
import org.springframework.stereotype.Component;

import java.util.Arrays;

@Component
public class ServiceValidator {

    public void validateServiceType(String serviceType) {
        try {
            ServiceType.valueOf(serviceType);
        } catch (IllegalArgumentException e) {
            throw new BusinessException(ErrorCode.VALIDATION_ERROR,
                    "Invalid service type: " + serviceType + ". Allowed values: " +
                            Arrays.toString(ServiceType.values()));
        }
    }

    public void validateVehicleType(String vehicleType) {
        try {
            VehicleType.valueOf(vehicleType);
        } catch (IllegalArgumentException e) {
            throw new BusinessException(ErrorCode.VALIDATION_ERROR,
                    "Invalid vehicle type: " + vehicleType + ". Allowed values: " +
                            Arrays.toString(VehicleType.values()));
        }
    }

    public void validateProviderType(String providerType) {
        try {
            ProviderType.valueOf(providerType);
        } catch (IllegalArgumentException e) {
            throw new BusinessException(ErrorCode.VALIDATION_ERROR,
                    "Invalid provider type: " + providerType + ". Allowed values: " +
                            Arrays.toString(ProviderType.values()));
        }
    }

    public void validatePrice(Double price) {
        if (price == null || price <= 0) {
            throw new BusinessException(ErrorCode.VALIDATION_ERROR, "Price must be greater than 0");
        }
    }

    public void validateDuration(Integer duration) {
        if (duration != null && duration <= 0) {
            throw new BusinessException(ErrorCode.VALIDATION_ERROR, "Duration must be greater than 0");
        }
    }
}
