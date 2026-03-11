package com.vyxentra.vehicle.service;


import com.vyxentra.vehicle.dto.response.DashboardResponse;

public interface DashboardService {

    DashboardResponse getDashboard();

    DashboardResponse refreshDashboard();
}
