package com.vyxentra.vehicle.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public  class PromotionalRate {
    private String promoId;
    private String promoName;
    private String promoCode;
    private Double commissionPercentage;
    private LocalDate validFrom;
    private LocalDate validTo;
    private Integer maxProviders;
    private Integer currentProviders;
    private Boolean isActive;
}
