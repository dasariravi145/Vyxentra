package com.vyxentra.vehicle.dto.request;


import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SendPushRequest {

    @NotBlank(message = "Title is required")
    private String title;

    @NotBlank(message = "Body is required")
    private String body;

    private Map<String, String> data; // Custom data payload

    private String imageUrl;

    private String clickAction;

    private String sound;

    private Integer priority; // 1-10

    private String referenceId;

    private String referenceType;
}
