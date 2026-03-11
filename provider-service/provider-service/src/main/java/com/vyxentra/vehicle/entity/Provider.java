package com.vyxentra.vehicle.entity;

import com.vyxentra.vehicle.enums.ProviderStatus;
import com.vyxentra.vehicle.enums.ProviderType;
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

import java.time.Instant;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Entity
@Table(name = "providers", indexes = {
        @Index(name = "idx_provider_status", columnList = "status"),
        @Index(name = "idx_provider_type", columnList = "provider_type"),
        @Index(name = "idx_provider_location", columnList = "latitude, longitude"),
        @Index(name = "idx_provider_user", columnList = "user_id")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class Provider {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(name = "user_id", nullable = false, unique = true)
    private String userId;

    @Column(name = "business_name", nullable = false)
    private String businessName;

    @Enumerated(EnumType.STRING)
    @Column(name = "provider_type", nullable = false)
    private ProviderType providerType;

    @Column(name = "owner_name")
    private String ownerName;

    @Column(name = "gst_number", unique = true)
    private String gstNumber;

    @Column(name = "pan_number")
    private String panNumber;

    @Column(name = "registration_number")
    private String registrationNumber;

    @Column(nullable = false)
    private String email;

    @Column(nullable = false)
    private String phone;

    @Column(name = "alternate_phone")
    private String alternatePhone;

    @Column(name = "address_line1", nullable = false)
    private String addressLine1;

    @Column(name = "address_line2")
    private String addressLine2;

    @Column(nullable = false)
    private String city;

    @Column(nullable = false)
    private String state;

    @Column(name = "postal_code", nullable = false)
    private String postalCode;

    @Column(nullable = false)
    private String country;

    @Column(nullable = false)
    private Double latitude;

    @Column(nullable = false)
    private Double longitude;

    @Column(name = "business_description", length = 1000)
    private String businessDescription;

    private String website;

    @Column(name = "year_of_establishment")
    private Integer yearOfEstablishment;

    @Column(name = "employee_count")
    private Integer employeeCount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ProviderStatus status;

    @Column(name = "suspension_reason")
    private String suspensionReason;

    @Column(name = "supports_bike", nullable = false)
    private Boolean supportsBike;

    @Column(name = "supports_car", nullable = false)
    private Boolean supportsCar;

    @Column(name = "opening_time")
    private LocalTime openingTime;

    @Column(name = "closing_time")
    private LocalTime closingTime;

    @Column(name = "working_days")
    private String workingDays;

    @Column(name = "twenty_four_seven")
    private Boolean twentyFourSeven;

    @Column(name = "average_rating")
    private Double averageRating;

    @Column(name = "total_reviews")
    private Integer totalReviews;

    @Column(name = "total_bookings")
    private Integer totalBookings;

    @Column(name = "completion_rate")
    private Double completionRate;

    @Column(name = "is_verified")
    private Boolean isVerified;

    @Column(name = "approved_by")
    private String approvedBy;

    @Column(name = "approved_at")
    private Instant approvedAt;

    @Column(name = "rejection_reason")
    private String rejectionReason;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "operating_hours", columnDefinition = "jsonb")
    private Map<String, Object> operatingHours;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "bank_details", columnDefinition = "jsonb")
    private Map<String, Object> bankDetails;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "insurance_details", columnDefinition = "jsonb")
    private Map<String, Object> insuranceDetails;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "metadata", columnDefinition = "jsonb")
    private Map<String, Object> metadata;

    @OneToMany(mappedBy = "provider", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private List<ProviderPricing> pricings = new ArrayList<>();

    @OneToMany(mappedBy = "provider", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private List<ProviderDocument> documents = new ArrayList<>();

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @Column(name = "created_by")
    private String createdBy;

    @Column(name = "updated_by")
    private String updatedBy;

    @PrePersist
    protected void onCreate() {
        if (averageRating == null) averageRating = 0.0;
        if (totalReviews == null) totalReviews = 0;
        if (totalBookings == null) totalBookings = 0;
        if (completionRate == null) completionRate = 0.0;
        if (isVerified == null) isVerified = false;
    }
}