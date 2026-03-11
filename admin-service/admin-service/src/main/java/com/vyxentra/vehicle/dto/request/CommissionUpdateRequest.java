package com.vyxentra.vehicle.dto.request;


import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
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
public class CommissionUpdateRequest {

    @NotNull(message = "Commission percentage is required")
    @Min(value = 0, message = "Commission must be between 0 and 100")
    @Max(value = 100, message = "Commission must be between 0 and 100")
    private Double commissionPercentage;

    @Positive(message = "Minimum commission must be positive")
    private Double minCommission;

    @Positive(message = "Maximum commission must be positive")
    private Double maxCommission;

    private LocalDate effectiveFrom;

    private LocalDate effectiveTo;

    private String reason;
}
