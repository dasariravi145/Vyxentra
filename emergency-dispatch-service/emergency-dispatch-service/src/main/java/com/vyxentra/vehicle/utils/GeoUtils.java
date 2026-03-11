package com.vyxentra.vehicle.utils;



import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

/**
 * Utility class for geographic calculations
 * Provides methods for distance calculation, bounding boxes, and location validation
 */
@Slf4j
@UtilityClass
public class GeoUtils {

    private static final double EARTH_RADIUS_KM = 6371.0;
    private static final double EARTH_RADIUS_MILES = 3959.0;
    private static final double KM_TO_MILES = 0.621371;

    /**
     * Calculate distance between two points using Haversine formula
     *
     * @param lat1 Latitude of first point in degrees
     * @param lon1 Longitude of first point in degrees
     * @param lat2 Latitude of second point in degrees
     * @param lon2 Longitude of second point in degrees
     * @return Distance in kilometers
     */
    public double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        validateCoordinates(lat1, lon1);
        validateCoordinates(lat2, lon2);

        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);

        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                        Math.sin(dLon / 2) * Math.sin(dLon / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return EARTH_RADIUS_KM * c;
    }

    /**
     * Calculate distance in miles
     */
    public double calculateDistanceInMiles(double lat1, double lon1, double lat2, double lon2) {
        return calculateDistance(lat1, lon1, lat2, lon2) * KM_TO_MILES;
    }

    /**
     * Check if point is within radius of center
     *
     * @param centerLat Center latitude
     * @param centerLon Center longitude
     * @param pointLat Point latitude
     * @param pointLon Point longitude
     * @param radiusKm Radius in kilometers
     * @return true if point is within radius
     */
    public boolean isWithinRadius(double centerLat, double centerLon,
                                  double pointLat, double pointLon, double radiusKm) {
        double distance = calculateDistance(centerLat, centerLon, pointLat, pointLon);
        return distance <= radiusKm;
    }

    /**
     * Calculate bounding box for given center and radius
     * Returns array [minLat, maxLat, minLon, maxLon]
     *
     * @param latitude Center latitude
     * @param longitude Center longitude
     * @param radiusKm Radius in kilometers
     * @return double array with [minLat, maxLat, minLon, maxLon]
     */
    public double[] calculateBoundingBox(double latitude, double longitude, double radiusKm) {
        validateCoordinates(latitude, longitude);

        double latChange = Math.toDegrees(radiusKm / EARTH_RADIUS_KM);
        double lonChange = Math.toDegrees(radiusKm / (EARTH_RADIUS_KM * Math.cos(Math.toRadians(latitude))));

        double minLat = Math.max(-90, latitude - latChange);
        double maxLat = Math.min(90, latitude + latChange);
        double minLon = Math.max(-180, longitude - lonChange);
        double maxLon = Math.min(180, longitude + lonChange);

        return new double[]{minLat, maxLat, minLon, maxLon};
    }

    /**
     * Calculate bounding box as an object
     */
    public BoundingBox getBoundingBox(double latitude, double longitude, double radiusKm) {
        double[] bbox = calculateBoundingBox(latitude, longitude, radiusKm);
        return new BoundingBox(bbox[0], bbox[1], bbox[2], bbox[3]);
    }

    /**
     * Calculate approximate distance for quick filtering (less accurate but faster)
     * Uses equirectangular approximation
     */
    public double calculateApproximateDistance(double lat1, double lon1, double lat2, double lon2) {
        double x = Math.toRadians(lon2 - lon1) * Math.cos(Math.toRadians((lat1 + lat2) / 2));
        double y = Math.toRadians(lat2 - lat1);
        return Math.sqrt(x * x + y * y) * EARTH_RADIUS_KM;
    }

    /**
     * Validate geographic coordinates
     *
     * @param latitude Latitude to validate
     * @param longitude Longitude to validate
     * @throws IllegalArgumentException if coordinates are invalid
     */
    public void validateCoordinates(double latitude, double longitude) {
        if (latitude < -90 || latitude > 90) {
            throw new IllegalArgumentException("Invalid latitude: " + latitude + ". Must be between -90 and 90");
        }
        if (longitude < -180 || longitude > 180) {
            throw new IllegalArgumentException("Invalid longitude: " + longitude + ". Must be between -180 and 180");
        }
    }

    /**
     * Check if coordinates are valid
     */
    public boolean areValidCoordinates(double latitude, double longitude) {
        return latitude >= -90 && latitude <= 90 && longitude >= -180 && longitude <= 180;
    }

    /**
     * Generate a geohash for given coordinates
     * Simple implementation - in production use a proper geohash library
     *
     * @param latitude Latitude in degrees
     * @param longitude Longitude in degrees
     * @param precision Number of characters (1-12)
     * @return Geohash string
     */
    public String generateGeoHash(double latitude, double longitude, int precision) {
        validateCoordinates(latitude, longitude);
        precision = Math.min(12, Math.max(1, precision));

        // Simple geohash implementation for demo
        // In production, use a library like geohash-java
        String latStr = String.format("%.6f", latitude + 90).replace(".", "");
        String lonStr = String.format("%.6f", longitude + 180).replace(".", "");

        StringBuilder hash = new StringBuilder();
        for (int i = 0; i < precision && i < latStr.length() && i < lonStr.length(); i++) {
            hash.append(latStr.charAt(i)).append(lonStr.charAt(i));
        }

        return hash.toString().substring(0, Math.min(precision * 2, hash.length()));
    }

    /**
     * Calculate the midpoint between two coordinates
     */
    public double[] calculateMidpoint(double lat1, double lon1, double lat2, double lon2) {
        double dLon = Math.toRadians(lon2 - lon1);

        double x1 = Math.cos(Math.toRadians(lat2)) * Math.cos(dLon);
        double y1 = Math.cos(Math.toRadians(lat2)) * Math.sin(dLon);

        double x2 = Math.cos(Math.toRadians(lat1));
        double y2 = 0;

        double x = (x1 + x2) / 2;
        double y = (y1 + y2) / 2;

        double lon = Math.toDegrees(Math.atan2(y, x));
        double lat = Math.toDegrees(Math.atan2(
                Math.sin(Math.toRadians(lat1)) + Math.sin(Math.toRadians(lat2)),
                Math.sqrt((Math.cos(Math.toRadians(lat1)) + x) * (Math.cos(Math.toRadians(lat1)) + x) + y * y)
        ));

        return new double[]{lat, lon};
    }

    /**
     * Calculate destination point given start point, bearing and distance
     *
     * @param latitude Start latitude
     * @param longitude Start longitude
     * @param bearing Bearing in degrees (0-360, 0 = North)
     * @param distanceKm Distance in kilometers
     * @return [destinationLat, destinationLon]
     */
    public double[] calculateDestination(double latitude, double longitude,
                                         double bearing, double distanceKm) {
        double angularDistance = distanceKm / EARTH_RADIUS_KM;
        double bearingRad = Math.toRadians(bearing);

        double lat1 = Math.toRadians(latitude);
        double lon1 = Math.toRadians(longitude);

        double lat2 = Math.asin(
                Math.sin(lat1) * Math.cos(angularDistance) +
                        Math.cos(lat1) * Math.sin(angularDistance) * Math.cos(bearingRad)
        );

        double lon2 = lon1 + Math.atan2(
                Math.sin(bearingRad) * Math.sin(angularDistance) * Math.cos(lat1),
                Math.cos(angularDistance) - Math.sin(lat1) * Math.sin(lat2)
        );

        return new double[]{Math.toDegrees(lat2), Math.toDegrees(lon2)};
    }

    /**
     * Calculate bearing between two points
     *
     * @return Bearing in degrees (0-360, 0 = North)
     */
    public double calculateBearing(double lat1, double lon1, double lat2, double lon2) {
        double lat1Rad = Math.toRadians(lat1);
        double lat2Rad = Math.toRadians(lat2);
        double dLon = Math.toRadians(lon2 - lon1);

        double y = Math.sin(dLon) * Math.cos(lat2Rad);
        double x = Math.cos(lat1Rad) * Math.sin(lat2Rad) -
                Math.sin(lat1Rad) * Math.cos(lat2Rad) * Math.cos(dLon);

        double bearing = Math.toDegrees(Math.atan2(y, x));
        return (bearing + 360) % 360;
    }

    /**
     * Format distance in human-readable format
     */
    public String formatDistance(double distanceKm) {
        if (distanceKm < 1) {
            return String.format("%.0f m", distanceKm * 1000);
        } else if (distanceKm < 10) {
            return String.format("%.1f km", distanceKm);
        } else {
            return String.format("%.0f km", distanceKm);
        }
    }

    /**
     * Estimate travel time based on distance and average speed
     *
     * @param distanceKm Distance in kilometers
     * @param averageSpeedKmh Average speed in km/h
     * @return Estimated time in minutes
     */
    public int estimateTravelTime(double distanceKm, double averageSpeedKmh) {
        if (averageSpeedKmh <= 0) {
            averageSpeedKmh = 30; // Default city speed
        }
        double timeHours = distanceKm / averageSpeedKmh;
        return (int) Math.ceil(timeHours * 60);
    }

    /**
     * Calculate zoom level for map based on radius
     */
    public int calculateZoomLevel(double radiusKm) {
        if (radiusKm <= 0.5) return 15;
        if (radiusKm <= 1) return 14;
        if (radiusKm <= 2) return 13;
        if (radiusKm <= 5) return 12;
        if (radiusKm <= 10) return 11;
        if (radiusKm <= 20) return 10;
        if (radiusKm <= 50) return 9;
        if (radiusKm <= 100) return 8;
        return 7;
    }

    /**
     * Inner class representing a bounding box
     */
    @lombok.Data
    @lombok.AllArgsConstructor
    public static class BoundingBox {
        private final double minLat;
        private final double maxLat;
        private final double minLon;
        private final double maxLon;

        public boolean contains(double latitude, double longitude) {
            return latitude >= minLat && latitude <= maxLat &&
                    longitude >= minLon && longitude <= maxLon;
        }

        public double getWidth() {
            return maxLon - minLon;
        }

        public double getHeight() {
            return maxLat - minLat;
        }

        public double getCenterLat() {
            return (minLat + maxLat) / 2;
        }

        public double getCenterLon() {
            return (minLon + maxLon) / 2;
        }
    }
}
