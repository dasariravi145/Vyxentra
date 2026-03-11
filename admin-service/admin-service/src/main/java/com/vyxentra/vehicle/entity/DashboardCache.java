package com.vyxentra.vehicle.entity;

import com.vyxentra.vehicle.dto.response.DashboardResponse;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;
import java.time.LocalDateTime;

@Entity
@Table(name = "dashboard_cache", indexes = {
        @Index(name = "idx_cache_key", columnList = "cache_key", unique = true),
        @Index(name = "idx_cache_expiry", columnList = "expires_at"),
        @Index(name = "idx_cache_created", columnList = "created_at")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class DashboardCache {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(name = "cache_key", nullable = false, unique = true)
    private String cacheKey;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "cache_data", nullable = false, columnDefinition = "jsonb")
    private DashboardResponse cacheData;

    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    @Column(name = "created_by")
    private String createdBy;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "version")
    private Integer version;

    @Column(name = "cache_size_bytes")
    private Long cacheSizeBytes;

    @Column(name = "access_count")
    private Long accessCount;

    @Column(name = "last_accessed_at")
    private LocalDateTime lastAccessedAt;

    @PrePersist
    protected void onCreate() {
        if (version == null) version = 1;
        if (accessCount == null) accessCount = 0L;
        if (cacheSizeBytes == null && cacheData != null) {
            try {
                // Approximate size calculation
                cacheSizeBytes = (long) cacheData.toString().length();
            } catch (Exception e) {
                cacheSizeBytes = 0L;
            }
        }
    }

    /**
     * Check if cache is expired
     */
    public boolean isExpired() {
        return expiresAt != null && expiresAt.isBefore(LocalDateTime.now());
    }

    /**
     * Check if cache is still valid
     */
    public boolean isValid() {
        return !isExpired();
    }

    /**
     * Increment access count
     */
    public void incrementAccessCount() {
        this.accessCount = (this.accessCount == null ? 1 : this.accessCount + 1);
        this.lastAccessedAt = LocalDateTime.now();
    }

    /**
     * Extend expiry by minutes
     */
    public void extendExpiry(int minutes) {
        if (this.expiresAt != null) {
            this.expiresAt = this.expiresAt.plusMinutes(minutes);
        }
    }

    /**
     * Set expiry from now plus minutes
     */
    public void setExpiryFromNow(int minutes) {
        this.expiresAt = LocalDateTime.now().plusMinutes(minutes);
    }
}
