package com.vyxentra.vehicle.dto.request;


import com.vyxentra.vehicle.enums.ProviderStatus;
import com.vyxentra.vehicle.enums.ProviderType;
import com.vyxentra.vehicle.enums.ServiceType;
import com.vyxentra.vehicle.enums.VehicleType;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProviderSearchRequest {

    @Min(value = -90, message = "Invalid latitude")
    @Max(value = 90, message = "Invalid latitude")
    private Double latitude;

    @Min(value = -180, message = "Invalid longitude")
    @Max(value = 180, message = "Invalid longitude")
    private Double longitude;

    @Min(value = 1, message = "Radius must be at least 1 km")
    @Max(value = 100, message = "Radius cannot exceed 100 km")
    private Integer radiusKm;

    private ProviderType providerType;
    private List<ProviderStatus> statuses;
    private VehicleType vehicleType;
    private ServiceType serviceType;

    @Min(value = 1, message = "Min rating must be between 1 and 5")
    @Max(value = 5, message = "Min rating must be between 1 and 5")
    private Double minRating;

    private Boolean isOpenNow;
    private LocalDateTime requestedTime;

    private String city;
    private String state;
    private String searchTerm;

    @Min(value = 0, message = "Page must be at least 0")
    private Integer page;

    @Min(value = 1, message = "Size must be at least 1")
    @Max(value = 100, message = "Size cannot exceed 100")
    private Integer size;

    private String sortBy;
    private String sortDirection;

    private Boolean supportsEmergency;
    private Boolean isVerified;

    @Min(value = 0, message = "Min price must be non-negative")
    private Double minPrice;

    @Min(value = 0, message = "Max price must be non-negative")
    private Double maxPrice;

    public boolean hasLocation() {
        return latitude != null && longitude != null && radiusKm != null;
    }
}
