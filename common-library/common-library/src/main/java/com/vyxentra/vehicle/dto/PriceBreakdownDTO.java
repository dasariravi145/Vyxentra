package com.vyxentra.vehicle.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PriceBreakdownDTO {

    private BigDecimal subtotal;
    private BigDecimal taxAmount;
    private BigDecimal platformFee;
    private BigDecimal emergencyCharge;
    private BigDecimal discountAmount;
    private BigDecimal totalAmount;

    @Builder.Default
    private List<PartItemDTO> parts = new ArrayList<>();

    @Builder.Default
    private List<ServiceItemDTO> services = new ArrayList<>();

    private BigDecimal partsTotal;
    private BigDecimal servicesTotal;

    // Tax breakdown
    private BigDecimal cgst;
    private BigDecimal sgst;
    private BigDecimal igst;

    // Commission
    private BigDecimal platformCommission;
    private BigDecimal providerEarnings;

    public void calculateTotals() {
        this.partsTotal = parts.stream()
                .map(PartItemDTO::getTotalPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        this.servicesTotal = services.stream()
                .map(ServiceItemDTO::getPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        this.subtotal = partsTotal.add(servicesTotal);
        this.totalAmount = subtotal
                .add(taxAmount)
                .add(platformFee)
                .add(emergencyCharge)
                .subtract(discountAmount);
    }
}
