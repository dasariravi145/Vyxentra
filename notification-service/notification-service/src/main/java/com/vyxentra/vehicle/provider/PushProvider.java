package com.vyxentra.vehicle.provider;


import java.util.Map;

public interface PushProvider {

    Map<String, Object> sendPush(String deviceToken, String title, String body,
                                 Map<String, String> data, String imageUrl, String clickAction);

    String getProviderName();
}
