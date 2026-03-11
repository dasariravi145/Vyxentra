package com.vyxentra.vehicle.dto.request;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SendBatchNotificationRequest {

    private List<String> userIds;
    private List<String> userTypes;
    private String channel; // EMAIL, SMS, PUSH
    private String templateName;
    private Map<String, Object> templateData;
    private Map<String, Object> overrideData;
}