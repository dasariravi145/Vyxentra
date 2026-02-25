package com.vyxentra.vehicle.util;

import com.vyxentra.vehicle.constants.ApiConstants;
import com.vyxentra.vehicle.dto.PartItemDTO;
import com.vyxentra.vehicle.dto.PriceBreakdownDTO;
import com.vyxentra.vehicle.dto.ServiceItemDTO;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

public final class PriceCalculator {

    private static final BigDecimal ONE_HUNDRED = new BigDecimal("100");
    private static final int SCALE = 2;
    private static final RoundingMode ROUNDING_MODE = RoundingMode.HALF_UP;

    private PriceCalculator() {}

    /**
     * Calculate price breakdown for parts and services
     */
    public static PriceBreakdownDTO calculatePriceBreakdown(
            List<PartItemDTO> parts,
            List<ServiceItemDTO> services,
            boolean isEmergency,
            BigDecimal platformFeePercentage) {

        PriceBreakdownDTO breakdown = new PriceBreakdownDTO();

        // Calculate parts total
        BigDecimal partsTotal = parts.stream()
                .filter(PartItemDTO::isApproved)
                .map(PartItemDTO::getTotalPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        breakdown.setParts(parts.stream().filter(PartItemDTO::isApproved).toList());

        // Calculate services total
        BigDecimal servicesTotal = services.stream()
                .filter(ServiceItemDTO::isApproved)
                .map(ServiceItemDTO::getPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        breakdown.setServices(services.stream().filter(ServiceItemDTO::isApproved).toList());

        // Calculate subtotal
        BigDecimal subtotal = partsTotal.add(servicesTotal);
        breakdown.setPartsTotal(partsTotal);
        breakdown.setServicesTotal(servicesTotal);
        breakdown.setSubtotal(subtotal);

        // Calculate tax (GST)
        BigDecimal taxAmount = calculateGST(subtotal);
        breakdown.setTaxAmount(taxAmount);

        // Calculate CGST and SGST (assuming 9% each for intra-state)
        breakdown.setCgst(taxAmount.divide(new BigDecimal("2"), SCALE, ROUNDING_MODE));
        breakdown.setSgst(taxAmount.divide(new BigDecimal("2"), SCALE, ROUNDING_MODE));

        // Calculate emergency charge if applicable
        if (isEmergency) {
            BigDecimal emergencyCharge = subtotal
                    .multiply(new BigDecimal(ApiConstants.EMERGENCY_SURCHARGE_PERCENTAGE))
                    .divide(ONE_HUNDRED, SCALE, ROUNDING_MODE);
            breakdown.setEmergencyCharge(emergencyCharge);
        } else {
            breakdown.setEmergencyCharge(BigDecimal.ZERO);
        }

        // Calculate platform fee
        BigDecimal platformFee = subtotal
                .multiply(platformFeePercentage)
                .divide(ONE_HUNDRED, SCALE, ROUNDING_MODE);
        breakdown.setPlatformFee(platformFee);

        // Calculate total
        BigDecimal total = subtotal
                .add(taxAmount)
                .add(breakdown.getEmergencyCharge())
                .add(platformFee);
        breakdown.setTotalAmount(total);

        // Calculate platform commission and provider earnings
        BigDecimal platformCommission = subtotal
                .multiply(new BigDecimal(ApiConstants.PLATFORM_COMMISSION_PERCENTAGE))
                .divide(ONE_HUNDRED, SCALE, ROUNDING_MODE);
        breakdown.setPlatformCommission(platformCommission);
        breakdown.setProviderEarnings(subtotal.subtract(platformCommission));

        breakdown.calculateTotals();
        return breakdown;
    }

    /**
     * Calculate GST (18%)
     */
    public static BigDecimal calculateGST(BigDecimal amount) {
        return amount
                .multiply(new BigDecimal(ApiConstants.GST_PERCENTAGE))
                .divide(ONE_HUNDRED, SCALE, ROUNDING_MODE);
    }

    /**
     * Calculate platform fee
     */
    public static BigDecimal calculatePlatformFee(BigDecimal amount) {
        return amount
                .multiply(new BigDecimal(ApiConstants.PLATFORM_COMMISSION_PERCENTAGE))
                .divide(ONE_HUNDRED, SCALE, ROUNDING_MODE);
    }

    /**
     * Apply emergency multiplier
     */
    public static BigDecimal applyEmergencyMultiplier(BigDecimal amount, BigDecimal multiplier) {
        if (multiplier == null || multiplier.compareTo(BigDecimal.ONE) <= 0) {
            return amount;
        }
        return amount.multiply(multiplier).setScale(SCALE, ROUNDING_MODE);
    }

    /**
     * Calculate price with all applicable charges
     */
    public static BigDecimal calculateFinalPrice(
            BigDecimal baseAmount,
            boolean isEmergency,
            BigDecimal emergencyMultiplier,
            boolean includeGST,
            boolean includePlatformFee) {

        BigDecimal amount = baseAmount;

        // Apply emergency multiplier
        if (isEmergency && emergencyMultiplier != null) {
            amount = applyEmergencyMultiplier(amount, emergencyMultiplier);
        }

        // Add platform fee
        if (includePlatformFee) {
            BigDecimal platformFee = calculatePlatformFee(amount);
            amount = amount.add(platformFee);
        }

        // Add GST
        if (includeGST) {
            BigDecimal gst = calculateGST(amount);
            amount = amount.add(gst);
        }

        return amount.setScale(SCALE, ROUNDING_MODE);
    }

    /**
     * Validate that at least one item is approved
     */
    public static void validateAtLeastOneApproved(List<?> items, String itemType) {
        boolean hasApproved = items.stream()
                .filter(item -> {
                    if (item instanceof PartItemDTO) {
                        return ((PartItemDTO) item).isApproved();
                    } else if (item instanceof ServiceItemDTO) {
                        return ((ServiceItemDTO) item).isApproved();
                    }
                    return false;
                })
                .findAny()
                .isPresent();

        if (!hasApproved) {
            throw new IllegalArgumentException("At least one " + itemType + " must be approved");
        }
    }
}
