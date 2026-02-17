package com.cheftory.api.recipe.dto;

import com.cheftory.api.recipe.content.step.entity.RecipeStep;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * 공개 레시피 조리 단계 DTO (details[], start 의도적 미포함 — 유료 콘텐츠 보호)
 *
 * @param stepOrder 단계 순서
 * @param subtitle 소제목
 */
public record PublicStep(
        @JsonProperty("step_order") Integer stepOrder,
        @JsonProperty("subtitle") String subtitle) {

    public static PublicStep from(RecipeStep step) {
        return new PublicStep(step.getStepOrder(), step.getSubtitle());
    }
}
