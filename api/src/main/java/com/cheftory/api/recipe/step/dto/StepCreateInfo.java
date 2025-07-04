package com.cheftory.api.recipe.step.dto;

import lombok.*;

import java.util.UUID;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder(access = AccessLevel.PRIVATE)
public class StepCreateInfo {
    private String subtitle;
    private String description;
    private Double start;
    private Double end;

    private UUID recipeInfoId;
}
