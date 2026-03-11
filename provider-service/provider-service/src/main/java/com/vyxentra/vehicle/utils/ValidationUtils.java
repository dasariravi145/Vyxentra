package com.vyxentra.vehicle.utils;


import com.vyxentra.vehicle.dto.request.ProviderRegistrationRequest;
import com.vyxentra.vehicle.exception.BusinessException;
import com.vyxentra.vehicle.exception.ErrorCode;
import org.springframework.stereotype.Component;

import java.util.regex.Pattern;

@Component
public class ValidationUtils {

    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("^[A-Za-z0-9+_.-]+@(.+)$");
    private static final Pattern PHONE_PATTERN =
            Pattern.compile("^\\+?[1-9][0-9]{7,14}$");
    private static final Pattern GST_PATTERN =
            Pattern.compile("^[0-9]{2}[A-Z]{5}[0-9]{4}[A-Z]{1}[1-9A-Z]{1}[Z]{1}[0-9A-Z]{1}$");
    private static final Pattern PAN_PATTERN =
            Pattern.compile("^[A-Z]{5}[0-9]{4}[A-Z]{1}$");

    public void validateProviderRegistration(ProviderRegistrationRequest request) {
        if (!Boolean.TRUE.equals(request.getSupportsBike()) &&
                !Boolean.TRUE.equals(request.getSupportsCar())) {
            throw new BusinessException(ErrorCode.PROVIDER_INVALID_VEHICLE_SUPPORT);
        }

        validateEmail(request.getEmail());
        validatePhone(request.getPhone());
        validateGst(request.getGstNumber());

        if (request.getPanNumber() != null && !request.getPanNumber().isEmpty()) {
            validatePan(request.getPanNumber());
        }

        validateCoordinates(request.getLatitude(), request.getLongitude());
    }

    public void validateEmail(String email) {
        if (email == null || !EMAIL_PATTERN.matcher(email).matches()) {
            throw new BusinessException(ErrorCode.VALIDATION_ERROR, "Invalid email format");
        }
    }

    public void validatePhone(String phone) {
        if (phone == null || !PHONE_PATTERN.matcher(phone).matches()) {
            throw new BusinessException(ErrorCode.VALIDATION_ERROR, "Invalid phone number format");
        }
    }

    public void validateGst(String gst) {
        if (gst == null || !GST_PATTERN.matcher(gst).matches()) {
            throw new BusinessException(ErrorCode.VALIDATION_ERROR, "Invalid GST number format");
        }
    }

    public void validatePan(String pan) {
        if (!PAN_PATTERN.matcher(pan).matches()) {
            throw new BusinessException(ErrorCode.VALIDATION_ERROR, "Invalid PAN number format");
        }
    }

    public void validateCoordinates(Double latitude, Double longitude) {
        if (latitude == null || longitude == null) {
            throw new BusinessException(ErrorCode.VALIDATION_ERROR, "Coordinates are required");
        }
        if (latitude < -90 || latitude > 90) {
            throw new BusinessException(ErrorCode.VALIDATION_ERROR, "Invalid latitude");
        }
        if (longitude < -180 || longitude > 180) {
            throw new BusinessException(ErrorCode.VALIDATION_ERROR, "Invalid longitude");
        }
    }
}