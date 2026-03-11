package com.vyxentra.vehicle.dto.request;


import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SkillUpdateRequest {

    @NotBlank(message = "Skill name is required")
    private String name;

    private String category;

    private String description;
}