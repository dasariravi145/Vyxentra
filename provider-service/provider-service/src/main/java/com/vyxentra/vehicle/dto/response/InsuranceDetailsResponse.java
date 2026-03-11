package com.vyxentra.vehicle.dto.response;



import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InsuranceDetailsResponse {

    private String policyNumber;
    private String insurerName;
    private String coverageType;
    private Double coverageAmount;
    private String validFrom;
    private String validUntil;
    private boolean verified;
}
