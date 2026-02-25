package com.vyxentra.vehicle.util;

import com.vyxentra.vehicle.exceptions.ValidationException;
import org.apache.commons.lang3.StringUtils;

import java.util.Collection;
import java.util.regex.Pattern;

public final class ValidationUtil {

    private static final Pattern MOBILE_PATTERN = Pattern.compile("^[0-9]{10}$");
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9+_.-]+@(.+)$");
    private static final Pattern PINCODE_PATTERN = Pattern.compile("^[1-9][0-9]{5}$");
    private static final Pattern GST_PATTERN = Pattern.compile("^[0-9]{2}[A-Z]{5}[0-9]{4}[A-Z]{1}[1-9A-Z]{1}Z[0-9A-Z]{1}$");
    private static final Pattern PAN_PATTERN = Pattern.compile("^[A-Z]{5}[0-9]{4}[A-Z]{1}$");

    private ValidationUtil() {}

    public static void validateMobileNumber(String mobileNumber) {
        if (StringUtils.isBlank(mobileNumber) || !MOBILE_PATTERN.matcher(mobileNumber).matches()) {
            throw new ValidationException("Invalid mobile number. Must be 10 digits");
        }
    }

    public static void validateEmail(String email) {
        if (StringUtils.isNotBlank(email) && !EMAIL_PATTERN.matcher(email).matches()) {
            throw new ValidationException("Invalid email format");
        }
    }

    public static void validatePincode(String pincode) {
        if (StringUtils.isBlank(pincode) || !PINCODE_PATTERN.matcher(pincode).matches()) {
            throw new ValidationException("Invalid pincode. Must be 6 digits");
        }
    }

    public static void validateGST(String gst) {
        if (StringUtils.isBlank(gst) || !GST_PATTERN.matcher(gst).matches()) {
            throw new ValidationException("Invalid GST number format");
        }
    }

    public static void validatePAN(String pan) {
        if (StringUtils.isBlank(pan) || !PAN_PATTERN.matcher(pan).matches()) {
            throw new ValidationException("Invalid PAN number format");
        }
    }

    public static void validateOtp(String otp) {
        if (StringUtils.isBlank(otp) || otp.length() != 6 || !StringUtils.isNumeric(otp)) {
            throw new ValidationException("OTP must be 6 digits");
        }
    }

    public static void validateRequired(Object value, String fieldName) {
        if (value == null) {
            throw new ValidationException(fieldName + " is required");
        }

        if (value instanceof String && StringUtils.isBlank((String) value)) {
            throw new ValidationException(fieldName + " is required");
        }

        if (value instanceof Collection<?> && ((Collection<?>) value).isEmpty()) {
            throw new ValidationException(fieldName + " cannot be empty");
        }
    }

    public static void validatePositive(Number value, String fieldName) {
        if (value == null || value.doubleValue() <= 0) {
            throw new ValidationException(fieldName + " must be positive");
        }
    }

    public static void validateNonNegative(Number value, String fieldName) {
        if (value == null || value.doubleValue() < 0) {
            throw new ValidationException(fieldName + " must be non-negative");
        }
    }

    public static void validateRange(Number value, Number min, Number max, String fieldName) {
        if (value == null) {
            throw new ValidationException(fieldName + " is required");
        }

        double val = value.doubleValue();
        if (val < min.doubleValue() || val > max.doubleValue()) {
            throw new ValidationException(
                    String.format("%s must be between %s and %s", fieldName, min, max)
            );
        }
    }
}
