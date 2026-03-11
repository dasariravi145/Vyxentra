package com.vyxentra.vehicle.service;


import org.springframework.stereotype.Service;

@Service
public class DistanceCalculationServiceImpl implements DistanceCalculationService {

    private static final double EARTH_RADIUS_KM = 6371;

    @Override
    public double calculateDistance(double lat1, double lon1,
                                    double lat2, double lon2) {

        double latDistance = Math.toRadians(lat2 - lat1);
        double lonDistance = Math.toRadians(lon2 - lon1);

        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat1))
                * Math.cos(Math.toRadians(lat2))
                * Math.sin(lonDistance / 2)
                * Math.sin(lonDistance / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return EARTH_RADIUS_KM * c;
    }

    @Override
    public int calculateETA(double distanceKm) {

        double averageSpeed = 40.0;

        double hours = distanceKm / averageSpeed;

        return (int) Math.ceil(hours * 60);
    }

    @Override
    public int calculateETA(double distanceKm, String trafficCondition) {

        double speed = 40;

        if ("HIGH".equalsIgnoreCase(trafficCondition)) {
            speed = 25;
        } else if ("MEDIUM".equalsIgnoreCase(trafficCondition)) {
            speed = 35;
        }

        double hours = distanceKm / speed;

        return (int) Math.ceil(hours * 60);
    }

    @Override
    public int calculateETA(double lat1, double lon1,
                            double lat2, double lon2) {

        double distance = calculateDistance(lat1, lon1, lat2, lon2);

        return calculateETA(distance);
    }

    @Override
    public boolean isWithinRadius(double centerLat,
                                  double centerLon,
                                  double pointLat,
                                  double pointLon,
                                  double radiusKm) {

        double distance = calculateDistance(centerLat, centerLon,
                pointLat, pointLon);

        return distance <= radiusKm;
    }

    @Override
    public BoundingBox calculateBoundingBox(double latitude,
                                            double longitude,
                                            double radiusKm) {

        double latChange = radiusKm / 111;

        double lonChange = radiusKm /
                (111 * Math.cos(Math.toRadians(latitude)));

        return new BoundingBox(
                latitude - latChange,
                latitude + latChange,
                longitude - lonChange,
                longitude + lonChange
        );
    }
}