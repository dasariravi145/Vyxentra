package com.vyxentra.vehicle.service;

import com.vyxentra.vehicle.utils.GeoUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class ETAServiceImpl implements ETAService {

    @Value("${tracking.eta.traffic-enabled:false}")
    private boolean trafficEnabled;

    // Average speed in km/h for different scenarios
    private static final double URBAN_SPEED = 30.0;
    private static final double HIGHWAY_SPEED = 60.0;
    private static final double TRAFFIC_FACTOR = 1.5; // 50% slower in traffic

    @Override
    public Integer calculateETA(Double currentLat, Double currentLng, Double destLat, Double destLng) {
        double distance = calculateDistance(currentLat, currentLng, destLat, destLng);

        // Assume urban speed for ETA calculation
        double timeHours = distance / URBAN_SPEED;
        int minutes = (int) Math.ceil(timeHours * 60);

        return minutes;
    }

    @Override
    public Integer calculateETAWithTraffic(Double currentLat, Double currentLng,
                                           Double destLat, Double destLng, String city) {
        double distance = calculateDistance(currentLat, currentLng, destLat, destLng);

        // Determine if it's highway or urban based on distance
        double speed = distance > 10 ? HIGHWAY_SPEED : URBAN_SPEED;

        // Apply traffic factor if enabled
        if (trafficEnabled) {
            // In real implementation, would fetch traffic data from external service
            // For now, apply a simple factor
            speed = speed / TRAFFIC_FACTOR;
        }

        double timeHours = distance / speed;
        int minutes = (int) Math.ceil(timeHours * 60);

        return minutes;
    }

    @Override
    public Double calculateDistance(Double lat1, Double lng1, Double lat2, Double lng2) {
        return GeoUtil.calculateDistance(lat1, lng1, lat2, lng2);
    }
}
