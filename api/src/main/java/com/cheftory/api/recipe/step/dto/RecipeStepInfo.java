package com.cheftory.api.recipe.step.dto;

import com.cheftory.api.common.converter.StringListJsonConverter;
import com.cheftory.api.recipe.step.entity.RecipeStep;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.List;
import java.util.UUID;

@AllArgsConstructor(access = AccessLevel.PACKAGE)
@Builder(access = AccessLevel.PACKAGE)
@Getter
public class RecipeStepInfo {
    private UUID id;
    private Integer stepOrder;

    private String subtitle;

    private List<String> details;

    private Double start;
    private Double end;

    public static RecipeStepInfo from(RecipeStep step) {
        return RecipeStepInfo.builder()
                .id(step.getId())
                .stepOrder(step.getStepOrder())
                .subtitle(step.getSubtitle())
                .details(step.getDetails())
                .start(step.getStart())
                .end(step.getEnd())
                .build();
    }
}
