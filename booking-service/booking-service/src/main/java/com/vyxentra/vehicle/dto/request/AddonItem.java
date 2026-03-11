package com.vyxentra.vehicle.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public  class AddonItem {
    private String addonId;
    private String addonName;
    private Integer quantity;
    private Double unitPrice;
    private Double totalPrice;
    private Boolean isApproved;
}
