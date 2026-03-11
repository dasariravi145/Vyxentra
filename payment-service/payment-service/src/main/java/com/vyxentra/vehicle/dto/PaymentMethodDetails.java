package com.vyxentra.vehicle.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public  class PaymentMethodDetails {
    private String type; // CARD, UPI, NETBANKING

    // Card details
    private String lastFour;
    private String cardNetwork;
    private String cardType;
    private String cardIssuer;

    // UPI details
    private String vpa;
    private String upiApp;

    // Bank details
    private String bankName;
    private String accountNumber;

    // EMI details
    private Boolean isEmi;
    private Integer emiTenure;
    private Double emiAmount;
    private Double emiInterest;
}
