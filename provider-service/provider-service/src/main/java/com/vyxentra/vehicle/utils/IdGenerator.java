package com.vyxentra.vehicle.utils;



import java.util.UUID;

public class IdGenerator {

    public static String generateUUID() {
        return UUID.randomUUID().toString();
    }

    public static String generateProviderId() {
        return "PROV_" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    public static String generateShortId(String prefix) {
        return prefix + "_" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }
}
