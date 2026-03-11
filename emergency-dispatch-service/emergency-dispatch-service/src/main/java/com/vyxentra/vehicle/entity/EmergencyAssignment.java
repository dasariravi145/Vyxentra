package com.vyxentra.vehicle.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;

import java.time.LocalDateTime;

@Entity
@Table(name = "emergency_assignments")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmergencyAssignment {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "request_id", nullable = false)
    private EmergencyRequest request;

    @Column(name = "provider_id", nullable = false)
    private String providerId;

    @Column(name = "provider_name")
    private String providerName;

    @Column(name = "provider_phone")
    private String providerPhone;

    @Column(name = "provider_lat")
    private Double providerLat;

    @Column(name = "provider_lng")
    private Double providerLng;

    @Column(name = "distance_km")
    private Double distanceKm;

    @Column(name = "eta_minutes")
    private Integer etaMinutes;

    @Column(name = "booking_id")
    private String bookingId;

    @Column(nullable = false)
    private String status; // ACCEPTED, ARRIVED, COMPLETED, CANCELLED

    @Column(name = "accepted_at", nullable = false)
    private LocalDateTime acceptedAt;

    private LocalDateTime startedAt;

    private LocalDateTime completedAt;
    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}