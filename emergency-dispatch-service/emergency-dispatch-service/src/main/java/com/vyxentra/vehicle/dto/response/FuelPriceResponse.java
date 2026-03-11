package com.vyxentra.vehicle.dto.response;



import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class FuelPriceResponse {

    private String fuelType;
    private Double price;
    private String city;
    private LocalDate date;
    private String currency;
    private String source; // Added this field (API, CACHE, FALLBACK)
    private String unit; // per liter
    private Double previousPrice;
    private Double priceChange;
    private LocalDate lastUpdated;

    // Convenience method to check if this is fallback data
    public boolean isFallback() {
        return "FALLBACK".equalsIgnoreCase(source) || "FALLBACK_CUSTOM".equalsIgnoreCase(source);
    }

    public Double getPriceChangePercentage() {
        if (previousPrice != null && previousPrice > 0) {
            return ((price - previousPrice) / previousPrice) * 100;
        }
        return 0.0;
    }
}
