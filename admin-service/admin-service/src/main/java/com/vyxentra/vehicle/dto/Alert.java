package com.vyxentra.vehicle.dto;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public  class Alert {
    private String type; // WARNING, ERROR, INFO
    private String message;
    private String action;
    private LocalDateTime timestamp;
}