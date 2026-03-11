package com.vyxentra.vehicle.entity;

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
import java.util.Map;

@Entity
@Table(name = "provider_approvals")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class ProviderApproval {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "provider_id", nullable = false, unique = true)
    private Provider provider;

    @Column(name = "assigned_admin")
    private String assignedAdmin;

    @Column(name = "assigned_at")
    private Instant assignedAt;

    @Column(name = "reviewed_by")
    private String reviewedBy;

    @Column(name = "reviewed_at")
    private Instant reviewedAt;

    private String comments;

    @Column(name = "rejection_reason")
    private String rejectionReason;

    @Column(name = "custom_commission")
    private Double customCommission;

    private Integer priority;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "verification_checklist", columnDefinition = "jsonb")
    private Map<String, Boolean> verificationChecklist;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "verification_notes", columnDefinition = "jsonb")
    private Map<String, String> verificationNotes;

    @Column(name = "risk_level")
    private String riskLevel;

    @Column(name = "risk_score")
    private Double riskScore;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "risk_factors", columnDefinition = "jsonb")
    private Map<String, Object> riskFactors;

    @Column(name = "submitted_at")
    private Instant submittedAt;

    @Column(name = "expected_review_by")
    private Instant expectedReviewBy;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;
}