package com.vyxentra.vehicle.dto.request;


import com.vyxentra.vehicle.enums.VehicleType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AddVehicleRequest {

    @NotNull(message = "Vehicle type is required")
    private VehicleType vehicleType;

    @NotBlank(message = "Make is required")
    private String make;

    @NotBlank(message = "Model is required")
    private String model;

    @NotBlank(message = "Year is required")
    @Pattern(regexp = "^[0-9]{4}$", message = "Invalid year")
    private String year;

    @NotBlank(message = "Registration number is required")
    @Pattern(regexp = "^[A-Z]{2}[0-9]{1,2}[A-Z]{1,2}[0-9]{4}$",
            message = "Invalid registration number format")
    private String registrationNumber;

    private String color;

    private boolean isDefault;

    // Additional fields
    private String fuelType;
    private String transmissionType;
    private Integer engineCapacity;
}