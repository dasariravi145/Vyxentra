package com.vyxentra.vehicle.service;


public interface DistanceCalculationService {

    double calculateDistance(double lat1, double lon1, double lat2, double lon2);

    int calculateETA(double distanceKm);

    int calculateETA(double distanceKm, String trafficCondition);

    int calculateETA(double lat1, double lon1, double lat2, double lon2);

    boolean isWithinRadius(double centerLat, double centerLon,
                           double pointLat, double pointLon,
                           double radiusKm);

    BoundingBox calculateBoundingBox(double latitude,
                                     double longitude,
                                     double radiusKm);

    class BoundingBox {

        private double minLat;
        private double maxLat;
        private double minLon;
        private double maxLon;

        public BoundingBox(double minLat, double maxLat,
                           double minLon, double maxLon) {
            this.minLat = minLat;
            this.maxLat = maxLat;
            this.minLon = minLon;
            this.maxLon = maxLon;
        }

        public double getMinLat() { return minLat; }
        public double getMaxLat() { return maxLat; }
        public double getMinLon() { return minLon; }
        public double getMaxLon() { return maxLon; }
    }
}
