package com.vyxentra.vehicle.utils;




import com.vyxentra.vehicle.exceptions.BusinessException;
import com.vyxentra.vehicle.exceptions.ErrorCode;
import lombok.experimental.UtilityClass;
import org.apache.commons.lang3.StringUtils;

import java.util.regex.Pattern;

@UtilityClass
public class ValidationUtil {

    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("^[A-Za-z0-9+_.-]+@(.+)$");
    private static final Pattern PHONE_PATTERN =
            Pattern.compile("^\\+?[1-9][0-9]{7,14}$");
    private static final Pattern OTP_PATTERN =
            Pattern.compile("^[0-9]{6}$");

    public void validateEmail(String email) {
        if (StringUtils.isBlank(email) || !EMAIL_PATTERN.matcher(email).matches()) {
            throw new BusinessException(ErrorCode.VALIDATION_ERROR, "Invalid email format");
        }
    }

    public void validatePhone(String phone) {
        if (StringUtils.isBlank(phone) || !PHONE_PATTERN.matcher(phone).matches()) {
            throw new BusinessException(ErrorCode.VALIDATION_ERROR, "Invalid phone number format");
        }
    }

    public void validateOtp(String otp) {
        if (StringUtils.isBlank(otp) || !OTP_PATTERN.matcher(otp).matches()) {
            throw new BusinessException(ErrorCode.VALIDATION_ERROR, "OTP must be 6 digits");
        }
    }

    public void validateRequired(Object value, String fieldName) {
        if (value == null) {
            throw new BusinessException(ErrorCode.VALIDATION_ERROR, fieldName + " is required");
        }

        if (value instanceof String && StringUtils.isBlank((String) value)) {
            throw new BusinessException(ErrorCode.VALIDATION_ERROR, fieldName + " cannot be empty");
        }
    }

    public void validatePositive(Number value, String fieldName) {
        if (value == null || value.doubleValue() <= 0) {
            throw new BusinessException(ErrorCode.VALIDATION_ERROR, fieldName + " must be positive");
        }
    }

    public void validateRange(Number value, Number min, Number max, String fieldName) {
        if (value == null) {
            return;
        }

        if (value.doubleValue() < min.doubleValue() || value.doubleValue() > max.doubleValue()) {
            throw new BusinessException(ErrorCode.VALIDATION_ERROR,
                    fieldName + " must be between " + min + " and " + max);
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