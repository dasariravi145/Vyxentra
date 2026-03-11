package com.vyxentra.vehicle.dto.response;


import com.vyxentra.vehicle.dto.ServiceHealth;
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
public class SystemHealthResponse {

    private String status; // UP, DEGRADED, DOWN
    private List<ServiceHealth> services;
    private Map<String, Object> database;
    private Map<String, Object> redis;
    private Map<String, Object> kafka;
    private Map<String, Object> diskSpace;
    private List<String> warnings;
    private List<String> errors;

}