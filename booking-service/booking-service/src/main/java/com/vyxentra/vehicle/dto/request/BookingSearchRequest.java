package com.vyxentra.vehicle.dto.request;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookingSearchRequest {

    private String customerId;
    private String providerId;
    private String employeeId;
    private List<String> statuses;
    private String vehicleType;
    private String serviceType;
    private Boolean isEmergency;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime fromDate;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime toDate;

    private Double minAmount;
    private Double maxAmount;

    private String searchTerm; // Search in booking number, customer name, etc.
}