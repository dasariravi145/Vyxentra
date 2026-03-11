package com.vyxentra.vehicle.entity;

import com.vyxentra.vehicle.enums.EmergencyType;
import com.vyxentra.vehicle.enums.VehicleType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;

@Entity
@Table(name = "emergency_requests")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class EmergencyRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(name = "request_number", nullable = false, unique = true)
    private String requestNumber;

    @Column(name = "customer_id", nullable = false)
    private String customerId;

    @Column(name = "customer_name")
    private String customerName;

    @Column(name = "customer_phone")
    private String customerPhone;

    @Enumerated(EnumType.STRING)
    @Column(name = "emergency_type", nullable = false)
    private EmergencyType emergencyType;

    @Enumerated(EnumType.STRING)
    @Column(name = "vehicle_type", nullable = false)
    private VehicleType vehicleType;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "vehicle_details", columnDefinition = "jsonb")
    private Map<String, Object> vehicleDetails;

    @Column(name = "location_lat", nullable = false)
    private Double locationLat;

    @Column(name = "location_lng", nullable = false)
    private Double locationLng;

    @Column(name = "location_address")
    private String locationAddress;

    @Column(nullable = false)
    private String status; // SEARCHING, ASSIGNED, EXPIRED, CANCELLED, COMPLETED

    @Column(name = "search_radius_km")
    private Integer searchRadiusKm;

    @Column(name = "current_radius_km")
    private Integer currentRadiusKm;

    @Column(name = "max_radius_km")
    private Integer maxRadiusKm;

    @Column(name = "expiry_time", nullable = false)
    private LocalDateTime expiryTime;

    // Repair specific
    @Column(name = "service_type")
    private String serviceType;

    @Column(name = "issue_description", length = 1000)
    private String issueDescription;

    // Petrol specific
    @Column(name = "fuel_type")
    private String fuelType;

    @Column(name = "quantity_liters")
    private Integer quantityLiters;

    @Column(name = "fuel_cost_per_liter")
    private Double fuelCostPerLiter;

    @Column(name = "total_fuel_cost")
    private BigDecimal totalFuelCost;

    // Amounts
    @Column(name = "base_amount")
    private Double baseAmount;

    private BigDecimal multiplier;

    @Column(name = "total_amount")
    private BigDecimal totalAmount;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}
