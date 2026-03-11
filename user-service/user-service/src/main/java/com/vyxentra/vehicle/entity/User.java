package com.vyxentra.vehicle.entity;


import com.vyxentra.vehicle.enums.Role;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "users")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class User {

    @Id
    private String id; // Same ID as auth-service

    @Column(nullable = false, unique = true)
    private String phoneNumber;

    @Column(nullable = false, unique = true)
    private String email;
    private String defaultVehicleId;
    @Column(nullable = false)
    private String fullName;

    private String alternatePhone;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    private String profilePicture;

    // Preferences
    private Boolean smsNotifications;
    private Boolean emailNotifications;
    private Boolean pushNotifications;
    private String preferredLanguage;

    // Stats
    private Integer totalBookings;
    private Integer totalSpent;
    private Double averageRating;

    // Provider specific fields
    private String businessName;
    private String providerStatus; // PENDING_APPROVAL, ACTIVE, SUSPENDED
    private Boolean supportsBike;
    private Boolean supportsCar;
    private Double latitude;
    private Double longitude;

    // Relationships
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @OrderBy("isDefault DESC, createdAt DESC")
    private List<Address> addresses = new ArrayList<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @OrderBy("isDefault DESC, createdAt DESC")
    private List<Vehicle> vehicles = new ArrayList<>();

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @LastModifiedDate
    @Column(nullable = false)
    private Instant updatedAt;

    private Instant lastLoginAt;

    @Column(nullable = false)
    private boolean active;


}