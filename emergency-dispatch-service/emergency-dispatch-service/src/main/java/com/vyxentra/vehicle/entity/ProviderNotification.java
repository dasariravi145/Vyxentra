package com.vyxentra.vehicle.entity;


import com.vyxentra.vehicle.enums.ProviderResponseStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "provider_notifications", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"request_id", "provider_id"})
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProviderNotification {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "request_id", nullable = false)
    private EmergencyRequest request;

    @Column(name = "provider_id", nullable = false)
    private String providerId;

    @Column(name = "notified_at", nullable = false)
    private LocalDateTime notifiedAt;

    @Column(name = "response_status")
    private ProviderResponseStatus responseStatus; // ACCEPTED, REJECTED, TIMEOUT

    @Column(name = "responded_at")
    private LocalDateTime respondedAt;
}
