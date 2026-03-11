package com.vyxentra.vehicle.dto.request;


import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SearchServicesRequest {

    @Size(min = 2, message = "Search query must be at least 2 characters")
    private String query;

    private String category;
    private String vehicleType;
    private String providerType;
    private Boolean isPopular;
    private Boolean isRecommended;
    private Double minPrice;
    private Double maxPrice;
    private String sortBy; // relevance, price_low, price_high, rating
    private Integer limit;
}
