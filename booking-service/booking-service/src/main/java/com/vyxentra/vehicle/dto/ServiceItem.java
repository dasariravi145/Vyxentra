package com.vyxentra.vehicle.dto;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public  class ServiceItem {
    private String serviceId;
    private String serviceName;
    private Integer quantity;
    private Double unitPrice;
    private Double totalPrice;
    private Boolean isApproved;
}
