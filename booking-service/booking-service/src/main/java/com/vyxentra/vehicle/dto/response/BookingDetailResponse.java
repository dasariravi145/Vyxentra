package com.vyxentra.vehicle.dto.response;

import com.vyxentra.vehicle.dto.Location;
import com.vyxentra.vehicle.dto.ServiceItem;
import com.vyxentra.vehicle.dto.TimelineEntry;
import com.vyxentra.vehicle.dto.request.AddonItem;
import com.vyxentra.vehicle.enums.BookingStatus;
import com.vyxentra.vehicle.enums.EmergencyType;
import com.vyxentra.vehicle.enums.ServiceType;
import com.vyxentra.vehicle.enums.VehicleType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookingDetailResponse {

    private String bookingId;
    private String bookingNumber;
    private String customerId;
    private String customerName;
    private String customerPhone;
    private String providerId;
    private String providerName;
    private String providerPhone;
    private String employeeId;
    private String employeeName;

    private VehicleType vehicleType;
    private Map<String, Object> vehicleDetails;
    private ServiceType serviceType;

    private BookingStatus status;
    private LocalDateTime scheduledTime;
    private LocalDateTime actualStartTime;
    private LocalDateTime actualEndTime;

    private Location location;

    private List<ServiceItem> services;
    private List<AddonItem> addons;

    private Double totalAmount;
    private Double approvedAmount;
    private Double paidAmount;
    private Double commissionAmount;
    private Double providerAmount;

    private Boolean isEmergency;
    private EmergencyType emergencyType;
    private Boolean upfrontPaymentRequired;
    private Boolean upfrontPaid;
    private String paymentStatus;

    private String cancellationReason;
    private String customerNotes;
    private String providerNotes;

    private Integer rating;
    private String review;

    private List<DamageReportResponse> damageReports;
    private List<TimelineEntry> timeline;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

}
