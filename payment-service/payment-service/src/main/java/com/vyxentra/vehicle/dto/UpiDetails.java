package com.vyxentra.vehicle.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public  class UpiDetails {
    private String vpa; // Virtual Payment Address
    private String intent; // Intent URL for UPI apps
}
