package com.vyxentra.vehicle.dto.response;



import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SkillResponse {

    private Long skillId;
    private String name;
    private String category;
    private String description;
    private Integer proficiencyLevel; // When returned as part of employee skills
    private Integer yearsOfExperience;
    private Boolean certified;
}
