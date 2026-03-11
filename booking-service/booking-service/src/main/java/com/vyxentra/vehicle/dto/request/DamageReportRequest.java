package com.vyxentra.vehicle.dto.request;


import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DamageReportRequest {

    @NotBlank(message = "Booking ID is required")
    private String bookingId;

    private String notes;

    private List<String> images;

    @NotEmpty(message = "At least one damage item is required")
    private List<DamageItemRequest> items;
}
