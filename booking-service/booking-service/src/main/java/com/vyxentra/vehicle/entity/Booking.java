package com.vyxentra.vehicle.entity;


import com.vyxentra.vehicle.enums.BookingStatus;
import com.vyxentra.vehicle.enums.EmergencyType;
import com.vyxentra.vehicle.enums.ServiceType;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Entity
@Table(name = "bookings")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class Booking {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(name = "booking_number", nullable = false, unique = true)
    private String bookingNumber;

    @Column(name = "customer_id", nullable = false)
    private String customerId;

    @Column(name = "provider_id", nullable = false)
    private String providerId;

    @Column(name = "employee_id")
    private String employeeId;

    @Enumerated(EnumType.STRING)
    @Column(name = "vehicle_type", nullable = false)
    private VehicleType vehicleType;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "vehicle_details", columnDefinition = "jsonb")
    private Map<String, Object> vehicleDetails;

    @Enumerated(EnumType.STRING)
    @Column(name = "service_type", nullable = false)
    private ServiceType serviceType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private BookingStatus status;

    @Column(name = "scheduled_time", nullable = false)
    private LocalDateTime scheduledTime;

    @Column(name = "actual_start_time")
    private LocalDateTime actualStartTime;

    @Column(name = "actual_end_time")
    private LocalDateTime actualEndTime;

    @Column(name = "location_lat")
    private Double locationLat;

    @Column(name = "location_lng")
    private Double locationLng;

    @Column(name = "location_address")
    private String locationAddress;

    @Column(name = "total_amount")
    private Double totalAmount;

    @Column(name = "approved_amount")
    private Double approvedAmount;

    @Column(name = "paid_amount")
    private Double paidAmount;

    @Column(name = "commission_amount")
    private Double commissionAmount;

    @Column(name = "provider_amount")
    private Double providerAmount;

    @Column(name = "is_emergency")
    private Boolean isEmergency;

    @Enumerated(EnumType.STRING)
    @Column(name = "emergency_type")
    private EmergencyType emergencyType;

    @Column(name = "upfront_payment_required")
    private Boolean upfrontPaymentRequired;

    @Column(name = "upfront_paid")
    private Boolean upfrontPaid;

    @Column(name = "payment_status")
    private String paymentStatus;

    @Column(name = "cancellation_reason", length = 1000)
    private String cancellationReason;

    @Column(name = "customer_notes", length = 1000)
    private String customerNotes;

    @Column(name = "provider_notes", length = 1000)
    private String providerNotes;

    private Integer rating;

    @Column(length = 1000)
    private String review;

    @Column(name = "snapshot_id")
    private String snapshotId;

    @OneToMany(mappedBy = "booking", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private List<BookingService> services = new ArrayList<>();

    @OneToMany(mappedBy = "booking", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private List<DamageReport> damageReports = new ArrayList<>();

    @OneToMany(mappedBy = "booking", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private List<BookingTimeline> timeline = new ArrayList<>();

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Column(name = "created_by")
    private String createdBy;

    @Column(name = "updated_by")
    private String updatedBy;
}
