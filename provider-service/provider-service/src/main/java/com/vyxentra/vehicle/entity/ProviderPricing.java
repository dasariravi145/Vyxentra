package com.vyxentra.vehicle.entity;


import com.vyxentra.vehicle.enums.PricingAlgorithm;
import com.vyxentra.vehicle.enums.ServiceType;
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
import java.time.Instant;
import java.util.Map;

@Entity
@Table(name = "provider_pricings", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"provider_id", "service_type"})
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class ProviderPricing {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "provider_id", nullable = false)
    private Provider provider;

    @Enumerated(EnumType.STRING)
    @Column(name = "service_type", nullable = false)
    private ServiceType serviceType;

    @Column(name = "base_price", nullable = false)
    private BigDecimal basePrice;

    private String currency;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "vehicle_pricing", columnDefinition = "jsonb")
    private Map<String, BigDecimal> vehiclePricing;  // Changed from Double to BigDecimal

    @Column(name = "estimated_duration_minutes")
    private Integer estimatedDurationMinutes;

    private String description;

    @Column(name = "is_active")
    private Boolean isActive;

    @Enumerated(EnumType.STRING)
    private PricingAlgorithm algorithm;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "dynamic_config", columnDefinition = "jsonb")
    private Map<String, Object> dynamicConfig;

    @Column(name = "tax_percentage")
    private BigDecimal taxPercentage;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private Map<String, Object> addons;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private Map<String, Object> metadata;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;
}
