package com.vyxentra.vehicle.entity;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "tracking_sessions")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class TrackingSession {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(name = "booking_id", nullable = false, unique = true)
    private String bookingId;

    @Column(name = "customer_id", nullable = false)
    private String customerId;

    @Column(name = "provider_id", nullable = false)
    private String providerId;

    @Column(name = "employee_id")
    private String employeeId;

    @Column(nullable = false)
    private String status; // ACTIVE, PAUSED, COMPLETED, EXPIRED

    @Column(name = "start_location_lat")
    private Double startLocationLat;

    @Column(name = "start_location_lng")
    private Double startLocationLng;

    @Column(name = "current_location_lat")
    private Double currentLocationLat;

    @Column(name = "current_location_lng")
    private Double currentLocationLng;

    @Column(name = "destination_lat", nullable = false)
    private Double destinationLat;

    @Column(name = "destination_lng", nullable = false)
    private Double destinationLng;

    @Column(name = "destination_address")
    private String destinationAddress;

    @Column(name = "started_at", nullable = false)
    private LocalDateTime startedAt;

    @Column(name = "last_update_at")
    private LocalDateTime lastUpdateAt;

    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @Column(name = "total_distance_km")
    private Double totalDistanceKm;

    @Column(name = "current_eta_minutes")
    private Integer currentEtaMinutes;

    @OneToMany(mappedBy = "trackingSession", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private List<ETAHistory> etaHistory = new ArrayList<>();

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}
