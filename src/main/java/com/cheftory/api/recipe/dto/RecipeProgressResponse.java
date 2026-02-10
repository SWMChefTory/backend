package com.cheftory.api.recipe.dto;

import com.cheftory.api.recipe.content.info.entity.RecipeStatus;
import com.cheftory.api.recipe.creation.progress.entity.RecipeProgressDetail;
import com.cheftory.api.recipe.creation.progress.entity.RecipeProgressStep;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

/**
 * 레시피 생성 진행률 응답 DTO.
 *
 * @param progressStatuses 진행 상태 목록
 * @param recipeStatus 레시피 상태
 */
public record RecipeProgressResponse(
        @JsonProperty("recipe_progress_statuses") List<ProgressStatus> progressStatuses,
        @JsonProperty("recipe_status") RecipeStatus recipeStatus) {

    /**
     * 진행 상태.
     *
     * @param progressDetail 진행 상세 정보
     * @param progressStep 현재 진행 단계
     */
    public record ProgressStatus(
            @JsonProperty("progress_detail") RecipeProgressDetail progressDetail,
            @JsonProperty("progress_step") RecipeProgressStep progressStep) {
        public static ProgressStatus of(RecipeProgressDetail recipeProgressDetail, RecipeProgressStep currentStep) {
            return new ProgressStatus(recipeProgressDetail, currentStep);
        }
    }

    /**
     * 레시피 진행 상태로부터 응답을 생성합니다.
     *
     * @param recipeProgressStatus 레시피 진행 상태
     * @return 레시피 진행률 응답
     */
    public static RecipeProgressResponse of(RecipeProgressStatus recipeProgressStatus) {
        List<ProgressStatus> progressStatuses = recipeProgressStatus.getProgresses().stream()
                .map(progress -> ProgressStatus.of(progress.getDetail(), progress.getStep()))
                .toList();

        return new RecipeProgressResponse(
                progressStatuses, recipeProgressStatus.getRecipe().getRecipeStatus());
    }
}
