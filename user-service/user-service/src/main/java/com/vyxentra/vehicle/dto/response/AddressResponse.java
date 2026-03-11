package com.vyxentra.vehicle.dto.response;


import com.vyxentra.vehicle.enums.AddressType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AddressResponse {

    private String addressId;
    private String addressLine1;
    private String addressLine2;
    private String city;
    private String state;
    private String postalCode;
    private String country;
    private AddressType addressType;
    private Double latitude;
    private Double longitude;
    private String landmark;
    private boolean isDefault;
    private String contactName;
    private String contactPhone;
    private Instant createdAt;
    private Instant updatedAt;
    private String last;
}
