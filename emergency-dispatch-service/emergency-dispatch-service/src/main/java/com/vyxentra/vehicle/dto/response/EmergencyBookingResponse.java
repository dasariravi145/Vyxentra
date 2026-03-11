package com.vyxentra.vehicle.dto.response;

import com.vyxentra.vehicle.dto.AssignmentInfo;
import com.vyxentra.vehicle.dto.FuelInfo;
import com.vyxentra.vehicle.dto.Location;
import com.vyxentra.vehicle.enums.EmergencyType;
import com.vyxentra.vehicle.enums.VehicleType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmergencyBookingResponse {

    private String requestId;
    private String requestNumber;
    private String customerId;
    private EmergencyType emergencyType;
    private VehicleType vehicleType;
    private Map<String, Object> vehicleDetails;

    private Location location;
    private String status; // SEARCHING, ASSIGNED, EXPIRED, CANCELLED, COMPLETED

    private Integer searchRadiusKm;
    private LocalDateTime expiryTime;

    // Provider info if assigned
    private AssignmentInfo assignment;

    // Petrol specific
    private FuelInfo fuelInfo;

    // Amounts
    private Double baseAmount;
    private BigDecimal multiplier;
    private BigDecimal totalAmount;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;



}
