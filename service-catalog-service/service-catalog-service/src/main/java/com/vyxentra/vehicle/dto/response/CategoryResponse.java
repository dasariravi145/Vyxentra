package com.vyxentra.vehicle.dto.response;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CategoryResponse {

    private String categoryId;
    private String name;
    private String description;
    private String icon;
    private Integer displayOrder;
    private Boolean isActive;
    private Integer serviceCount;
    private Instant createdAt;
    private Instant updatedAt;
}
