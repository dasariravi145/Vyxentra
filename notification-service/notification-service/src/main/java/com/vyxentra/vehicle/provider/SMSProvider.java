package com.vyxentra.vehicle.provider;


import java.util.Map;

public interface SMSProvider {

    Map<String, Object> sendSMS(String from, String to, String message);

    String getProviderName();
}
