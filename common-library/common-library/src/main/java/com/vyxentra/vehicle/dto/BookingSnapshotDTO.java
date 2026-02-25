package com.vyxentra.vehicle.dto;

import com.vyxentra.vehicle.enums.BookingStatus;
import com.vyxentra.vehicle.enums.ProviderType;
import com.vyxentra.vehicle.enums.VehicleType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookingSnapshotDTO {

    private String bookingId;
    private String bookingReference;

    // Customer snapshot
    private String customerId;
    private String customerName;
    private String customerMobile;
    private AddressDTO customerAddress;

    // Provider snapshot
    private String providerId;
    private String providerName;
    private ProviderType providerType;
    private GeoLocationDTO providerLocation;

    // Vehicle snapshot
    private VehicleType vehicleType;
    private String vehicleNumber;
    private String vehicleModel;

    // Service snapshot
    private String serviceId;
    private String serviceName;
    private BookingStatus status;
    private boolean isEmergency;

    // Pricing snapshot (immutable)
    private BigDecimal basePrice;
    private BigDecimal emergencyMultiplier;
    private BigDecimal platformFee;
    private BigDecimal taxAmount;
    private BigDecimal totalPrice;
    private Map<String, BigDecimal> priceComponents;

    // Damage/Repair snapshot (for service centers)
    private DamageReportDTO damageReport;
    private DamageApprovalDTO damageApproval;
    private PriceBreakdownDTO approvedPriceBreakdown;

    // Timestamps
    private Instant createdAt;
    private Instant assignedAt;
    private Instant startedAt;
    private Instant completedAt;
    private Instant approvedAt;

    // Metadata
    private String createdBy;
    private String assignedToEmployeeId;
    private String assignedToEmployeeName;
    private String cancellationReason;
    private Map<String, Object> metadata;
}
