package com.vyxentra.vehicle.dto.request;


import com.vyxentra.vehicle.dto.ApprovedItem;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DamageApprovalRequest {

    @NotEmpty(message = "At least one item must be approved")
    private List<ApprovedItem> approvedItems;

    private String notes;

}
