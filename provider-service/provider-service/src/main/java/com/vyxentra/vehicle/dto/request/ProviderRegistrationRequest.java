package com.vyxentra.vehicle.dto.request;


import com.vyxentra.vehicle.enums.ProviderType;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProviderRegistrationRequest {

    @NotBlank(message = "Business name is required")
    @Size(min = 2, max = 100, message = "Business name must be between 2 and 100 characters")
    private String businessName;

    @NotNull(message = "Provider type is required")
    private ProviderType providerType;

    @NotBlank(message = "Owner name is required")
    @Size(min = 2, max = 100, message = "Owner name must be between 2 and 100 characters")
    private String ownerName;

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    private String email;

    @NotBlank(message = "Phone number is required")
    @Pattern(regexp = "^\\+?[1-9][0-9]{7,14}$", message = "Invalid phone number format")
    private String phone;

    private String alternatePhone;

    @NotBlank(message = "GST number is required")
    @Pattern(regexp = "^[0-9]{2}[A-Z]{5}[0-9]{4}[A-Z]{1}[1-9A-Z]{1}[Z]{1}[0-9A-Z]{1}$",
            message = "Invalid GST number format")
    private String gstNumber;

    @Pattern(regexp = "^[A-Z]{5}[0-9]{4}[A-Z]{1}$", message = "Invalid PAN number format")
    private String panNumber;

    private String registrationNumber;

    @NotBlank(message = "Address line 1 is required")
    private String addressLine1;

    private String addressLine2;

    @NotBlank(message = "City is required")
    private String city;

    @NotBlank(message = "State is required")
    private String state;

    @NotBlank(message = "Postal code is required")
    @Pattern(regexp = "^[1-9][0-9]{5}$", message = "Invalid postal code")
    private String postalCode;

    @NotBlank(message = "Country is required")
    private String country;

    @NotNull(message = "Latitude is required")
    @Min(value = -90, message = "Invalid latitude")
    @Max(value = 90, message = "Invalid latitude")
    private Double latitude;

    @NotNull(message = "Longitude is required")
    @Min(value = -180, message = "Invalid longitude")
    @Max(value = 180, message = "Invalid longitude")
    private Double longitude;

    @NotNull(message = "Vehicle support information is required")
    private Boolean supportsBike;

    @NotNull(message = "Vehicle support information is required")
    private Boolean supportsCar;

    @Size(max = 500, message = "Description cannot exceed 500 characters")
    private String businessDescription;

    private String website;

    @Min(value = 1900, message = "Invalid year")
    @Max(value = 2100, message = "Invalid year")
    private Integer yearOfEstablishment;

    @Min(value = 1, message = "Employee count must be at least 1")
    private Integer employeeCount;

    private OperatingHours operatingHours;
    private BankDetails bankDetails;
    private List<DocumentUpload> documents;
    private InsuranceDetails insuranceDetails;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OperatingHours {
        private String monday;
        private String tuesday;
        private String wednesday;
        private String thursday;
        private String friday;
        private String saturday;
        private String sunday;
        private String timezone;
        private boolean twentyFourSeven;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BankDetails {
        @NotBlank(message = "Account holder name is required")
        private String accountHolderName;

        @NotBlank(message = "Account number is required")
        private String accountNumber;

        @NotBlank(message = "IFSC code is required")
        @Pattern(regexp = "^[A-Z]{4}0[A-Z0-9]{6}$", message = "Invalid IFSC code")
        private String ifscCode;

        private String bankName;
        private String branchName;
        private String accountType;
        private String upiId;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DocumentUpload {
        @NotBlank(message = "Document type is required")
        private String documentType;

        @NotBlank(message = "Document URL is required")
        private String documentUrl;

        private String documentNumber;
        private String expiryDate;
        private String issuedBy;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class InsuranceDetails {
        private String policyNumber;
        private String insurerName;
        private String coverageType;
        private Double coverageAmount;
        private String validFrom;
        private String validUntil;
        private String documentUrl;
    }
}
