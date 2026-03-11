package com.vyxentra.vehicle.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public  class TaxDetails {
    private Double tdsAmount;
    private Double tdsPercentage;
    private Double gstAmount;
    private Double gstPercentage;
    private String taxId;
    private String panNumber;
    private String gstNumber;
}
