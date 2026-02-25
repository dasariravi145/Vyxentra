package com.vyxentra.vehicle.util;

import com.vyxentra.vehicle.dto.GeoLocationDTO;

public final class GeoUtil {

    private static final double EARTH_RADIUS = 6371; // kilometers
    private static final double MIN_LATITUDE = -90;
    private static final double MAX_LATITUDE = 90;
    private static final double MIN_LONGITUDE = -180;
    private static final double MAX_LONGITUDE = 180;

    private GeoUtil() {}

    /**
     * Calculate distance between two points in kilometers using Haversine formula
     */
    public static double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);

        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                        Math.sin(dLon / 2) * Math.sin(dLon / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return EARTH_RADIUS * c;
    }

    public static double calculateDistance(GeoLocationDTO loc1, GeoLocationDTO loc2) {
        if (loc1 == null || loc2 == null || !loc1.isValid() || !loc2.isValid()) {
            throw new IllegalArgumentException("Invalid locations for distance calculation");
        }
        return calculateDistance(loc1.getLatitude(), loc1.getLongitude(),
                loc2.getLatitude(), loc2.getLongitude());
    }

    /**
     * Check if a point is within radius of center
     */
    public static boolean isWithinRadius(double centerLat, double centerLon,
                                         double pointLat, double pointLon,
                                         double radiusKm) {
        return calculateDistance(centerLat, centerLon, pointLat, pointLon) <= radiusKm;
    }

    /**
     * Generate bounding box coordinates for a given center and radius
     * Returns [minLat, maxLat, minLon, maxLon]
     */
    public static double[] getBoundingBox(double latitude, double longitude, double radiusKm) {
        double latRad = Math.toRadians(latitude);

        // Angular distance in radians
        double radDist = radiusKm / EARTH_RADIUS;

        double minLat = latitude - Math.toDegrees(radDist);
        double maxLat = latitude + Math.toDegrees(radDist);

        double minLon, maxLon;

        if (minLat > MIN_LATITUDE && maxLat < MAX_LATITUDE) {
            double deltaLon = Math.asin(Math.sin(radDist) / Math.cos(latRad));
            minLon = longitude - Math.toDegrees(deltaLon);
            maxLon = longitude + Math.toDegrees(deltaLon);

            if (minLon < MIN_LONGITUDE) minLon = MIN_LONGITUDE;
            if (maxLon > MAX_LONGITUDE) maxLon = MAX_LONGITUDE;
        } else {
            // Handle near poles
            minLat = Math.max(minLat, MIN_LATITUDE);
            maxLat = Math.min(maxLat, MAX_LATITUDE);
            minLon = MIN_LONGITUDE;
            maxLon = MAX_LONGITUDE;
        }

        return new double[]{minLat, maxLat, minLon, maxLon};
    }

    /**
     * Generate a geohash of specified precision
     * Simplified implementation - in production use a library like geohash-java
     */
    public static String generateGeoHash(double latitude, double longitude, int precision) {
        // This is a placeholder - in production, use a proper geohashing library
        return String.format("%.6f,%.6f", latitude, longitude);
    }

    /**
     * Validate coordinates
     */
    public static void validateCoordinates(double latitude, double longitude) {
        if (latitude < MIN_LATITUDE || latitude > MAX_LATITUDE) {
            throw new IllegalArgumentException("Latitude must be between -90 and 90");
        }
        if (longitude < MIN_LONGITUDE || longitude > MAX_LONGITUDE) {
            throw new IllegalArgumentException("Longitude must be between -180 and 180");
        }
    }
}
