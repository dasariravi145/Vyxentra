package com.vyxentra.vehicle.dto.response;

import com.vyxentra.vehicle.enums.BookingStatus;
import com.vyxentra.vehicle.enums.ServiceType;
import com.vyxentra.vehicle.enums.VehicleType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookingResponse {

    private String bookingId;
    private String bookingNumber;
    private String customerId;
    private String providerId;
    private String providerName;
    private String employeeId;
    private String employeeName;
    private VehicleType vehicleType;
    private ServiceType serviceType;
    private String serviceName;
    private BookingStatus status;
    private LocalDateTime scheduledTime;
    private String location;
    private Double totalAmount;
    private Double approvedAmount;
    private Boolean isEmergency;
    private Boolean upfrontPaymentRequired;
    private Boolean upfrontPaid;
    private String paymentStatus;
    private LocalDateTime createdAt;
}