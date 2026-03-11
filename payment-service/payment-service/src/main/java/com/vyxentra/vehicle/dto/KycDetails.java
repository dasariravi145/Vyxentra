package com.vyxentra.vehicle.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public  class KycDetails {
    private String panNumber;
    private String gstNumber;
    private String tanNumber;
    private String addressProof;
    private String identityProof;
    private Boolean isPanVerified;
    private Boolean isGstVerified;
    private Instant panVerifiedAt;
    private Instant gstVerifiedAt;
}
