package com.vyxentra.vehicle.dto.request;

import com.vyxentra.vehicle.enums.EmergencyType;
import com.vyxentra.vehicle.enums.ServiceType;
import com.vyxentra.vehicle.enums.VehicleType;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateBookingRequest {

    @NotBlank(message = "Provider ID is required")
    private String providerId;

    @NotNull(message = "Service type is required")
    private ServiceType serviceType;

    @NotNull(message = "Vehicle type is required")
    private VehicleType vehicleType;

    private Map<String, Object> vehicleDetails; // make, model, registration, etc.

    @NotNull(message = "Scheduled time is required")
    @Future(message = "Scheduled time must be in the future")
    private LocalDateTime scheduledTime;

    @NotNull(message = "Location is required")
    private Location location;

    private String customerNotes;

    private List<ServiceItem> services;

    private List<String> addonIds;

    // Emergency booking fields
    private Boolean isEmergency;
    private EmergencyType emergencyType;

    // Petrol emergency specific
    private PetrolDetails petrolDetails;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Location {
        @NotNull(message = "Latitude is required")
        private Double latitude;

        @NotNull(message = "Longitude is required")
        private Double longitude;

        private String address;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ServiceItem {
        private String serviceId;
        private String serviceName;
        private Integer quantity;
        private Double price;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PetrolDetails {
        @NotBlank(message = "Fuel type is required")
        private String fuelType; // PETROL, DIESEL

        @Positive(message = "Quantity must be positive")
        private Integer quantity; // in liters
    }
}
