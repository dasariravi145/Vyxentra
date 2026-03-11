package com.vyxentra.vehicle.service;


public interface ETAService {

    Integer calculateETA(Double currentLat, Double currentLng, Double destLat, Double destLng);

    Integer calculateETAWithTraffic(Double currentLat, Double currentLng,
                                    Double destLat, Double destLng, String city);

    Double calculateDistance(Double lat1, Double lng1, Double lat2, Double lng2);
}
