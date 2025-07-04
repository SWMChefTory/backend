package com.cheftory.api.recipe.step.dto;

import com.cheftory.api.recipe.step.entity.RecipeStep;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@AllArgsConstructor(access= AccessLevel.PRIVATE)
@Builder(access = AccessLevel.PRIVATE)
@Getter
public class RecipeStepFindResponse {
    private Integer order;
    private String subtitle;
    private String details;
    private Double start;
    private Double end;

    public static RecipeStepFindResponse of(RecipeStep step) {
        return RecipeStepFindResponse.builder()
                .order(step.getOrder())
                .subtitle(step.getSubtitle())
                .details(step.getDetails())
                .start(step.getStart())
                .end(step.getEnd())
                .build();
    }
}
