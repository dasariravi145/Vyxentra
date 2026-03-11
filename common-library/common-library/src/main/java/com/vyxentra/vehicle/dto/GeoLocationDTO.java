package com.vyxentra.vehicle.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.io.Serializable;

/**
 * Data Transfer Object for geographic location information
 * Used across all services for consistent location handling
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class GeoLocationDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * Latitude coordinate (-90 to 90)
     */
    @NotNull(message = "Latitude is required")
    @Min(value = -90, message = "Latitude must be between -90 and 90")
    @Max(value = 90, message = "Latitude must be between -90 and 90")
    private Double latitude;

    /**
     * Longitude coordinate (-180 to 180)
     */
    @NotNull(message = "Longitude is required")
    @Min(value = -180, message = "Longitude must be between -180 and 180")
    @Max(value = 180, message = "Longitude must be between -180 and 180")
    private Double longitude;

    /**
     * Human-readable address (optional)
     */
    private String address;

    /**
     * Additional location details
     */
    private String city;
    private String state;
    private String country;
    private String postalCode;

    /**
     * Place name or landmark
     */
    private String placeName;

    /**
     * Accuracy of the coordinates in meters
     */
    private Double accuracy;

    /**
     * Altitude in meters above sea level
     */
    private Double altitude;

    /**
     * Source of the location data (GPS, NETWORK, MANUAL, IP)
     */
    private String source;

    /**
     * Timestamp of the location fix
     */
    private Long timestamp;

    /**
     * Geohash for efficient spatial queries
     */
    private String geohash;

    /**
     * Check if coordinates are valid
     */
    public boolean isValid() {
        return latitude != null && longitude != null &&
                latitude >= -90 && latitude <= 90 &&
                longitude >= -180 && longitude <= 180;
    }

    /**
     * Calculate distance to another location in kilometers
     */
    public double distanceTo(GeoLocationDTO other) {
        if (other == null || !this.isValid() || !other.isValid()) {
            return -1;
        }
        return calculateDistance(this.latitude, this.longitude,
                other.getLatitude(), other.getLongitude());
    }

    /**
     * Haversine formula for distance calculation
     */
    private double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        final double EARTH_RADIUS_KM = 6371.0;

        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);

        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                        Math.sin(dLon / 2) * Math.sin(dLon / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return EARTH_RADIUS_KM * c;
    }

    /**
     * Get formatted coordinates string
     */
    public String toCoordinateString() {
        return String.format("%.6f,%.6f", latitude, longitude);
    }

    /**
     * Get Google Maps URL for this location
     */
    public String getGoogleMapsUrl() {
        return String.format("https://www.google.com/maps?q=%.6f,%.6f", latitude, longitude);
    }

    /**
     * Builder with convenience methods
     */
    public static class GeoLocationDTOBuilder {

        public GeoLocationDTOBuilder withCoordinates(double latitude, double longitude) {
            this.latitude = latitude;
            this.longitude = longitude;
            return this;
        }

        public GeoLocationDTOBuilder withAddress(String address, String city,
                                                 String state, String country,
                                                 String postalCode) {
            this.address = address;
            this.city = city;
            this.state = state;
            this.country = country;
            this.postalCode = postalCode;
            return this;
        }

        public GeoLocationDTOBuilder fromIp(String ip) {
            this.source = "IP";
            return this;
        }

        public GeoLocationDTOBuilder fromGps(double accuracy) {
            this.source = "GPS";
            this.accuracy = accuracy;
            return this;
        }

        public GeoLocationDTOBuilder fromNetwork() {
            this.source = "NETWORK";
            return this;
        }
    }
}
