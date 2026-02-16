package com.cheftory.api.recipe.content.step.client.dto;

import com.cheftory.api._common.Clock;
import com.cheftory.api.recipe.content.step.entity.RecipeStep;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import java.util.UUID;
import java.util.stream.IntStream;

/**
 * 외부 API로부터 받은 레시피 단계 정보 응답 DTO
 *
 * @param steps 단계 목록
 */
public record ClientRecipeStepsResponse(
        @JsonProperty("steps") @NotNull List<Step> steps) {

    /**
     * 개별 단계 정보 레코드
     *
     * @param subtitle 부제목
     * @param start 시작 시간
     * @param descriptions 상세 설명 목록
     */
    public record Step(
            @JsonProperty("subtitle") @NotNull String subtitle,
            @JsonProperty("start") Double start,
            @JsonProperty("descriptions") @NotNull List<Description> descriptions) {

        /**
         * 단계 상세 설명 레코드
         */
        private record Description(
                @JsonProperty("text") @NotNull String text,
                @JsonProperty("start") @NotNull Double start) {

            /**
             * 엔티티의 Detail 객체로 변환
             *
             * @return 단계 상세 엔티티 객체
             */
            private RecipeStep.Detail toRecipeStepDetail() {
                return RecipeStep.Detail.of(text, start);
            }
        }
    }

    /**
     * 응답 DTO를 RecipeStep 엔티티 목록으로 변환
     *
     * @param recipeId 레시피 ID
     * @param clock 현재 시간 제공 객체
     * @return 레시피 단계 엔티티 목록
     */
    public List<RecipeStep> toRecipeSteps(UUID recipeId, Clock clock) {
        return IntStream.range(0, steps.size())
                .mapToObj(i -> {
                    Step step = steps.get(i);
                    List<RecipeStep.Detail> details = step.descriptions().stream()
                            .map(Step.Description::toRecipeStepDetail)
                            .toList();
                    return RecipeStep.create(i + 1, step.subtitle(), details, step.start(), recipeId, clock);
                })
                .toList();
    }
}
