package com.vyxentra.vehicle.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public  class BookingDetail {
    @NotBlank(message = "Booking ID is required")
    private String bookingId;

    private String bookingNumber;

    @Positive(message = "Amount must be positive")
    private Double amount;

    private Double commission;

    private Double tax;

    private Double netAmount;

    private String serviceType;

    private LocalDate completedDate;
}

