package com.vyxentra.vehicle.events;



import com.vyxentra.vehicle.dto.DamageItem;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;
import java.util.List;

@Data
@SuperBuilder
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class DamageEvent extends BaseEvent {
    private String bookingId;
    private String damageReportId;
    private String employeeId;
    private List<DamageItem> damageItems;
    private BigDecimal totalAmount;
    private String status; // REPORTED, APPROVED, REJECTED
    private String notes;
    private List<String> imageUrls;
}