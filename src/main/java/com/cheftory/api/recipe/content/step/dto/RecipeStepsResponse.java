package com.cheftory.api.recipe.content.step.dto;

import com.cheftory.api.recipe.content.step.entity.RecipeStep;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import java.util.UUID;

/**
 * 레시피 단계 목록 응답 DTO
 *
 * @param steps 레시피 단계 목록
 */
public record RecipeStepsResponse(@JsonProperty("steps") List<RecipeStepResponse> steps) {

    /**
     * 엔티티 목록으로부터 응답 DTO 생성
     *
     * @param recipeSteps 단계 엔티티 목록
     * @return 레시피 단계 목록 응답 DTO
     */
    public static RecipeStepsResponse from(List<RecipeStep> recipeSteps) {
        List<RecipeStepResponse> stepResponses =
                recipeSteps.stream().map(RecipeStepResponse::from).toList();
        return new RecipeStepsResponse(stepResponses);
    }

    /**
     * 개별 레시피 단계 응답 레코드
     *
     * @param id 단계 ID
     * @param stepOrder 단계 순서
     * @param subtitle 부제목
     * @param details 상세 내용 목록
     * @param start 시작 시간
     */
    public record RecipeStepResponse(
            @JsonProperty("id") UUID id,
            @JsonProperty("step_order") Integer stepOrder,
            @JsonProperty("subtitle") String subtitle,
            @JsonProperty("details") List<RecipeStepDetailResponse> details,
            @JsonProperty("start") Double start) {

        /**
         * 단계 상세 내용 응답 레코드
         *
         * @param text 텍스트
         * @param start 시작 시간
         */
        public record RecipeStepDetailResponse(
                @JsonProperty("text") String text,
                @JsonProperty("start") Double start) {

            /**
             * 엔티티의 Detail 객체로부터 DTO 생성
             *
             * @param detail 단계 상세 엔티티 객체
             * @return 단계 상세 응답 DTO
             */
            public static RecipeStepDetailResponse from(RecipeStep.Detail detail) {
                return new RecipeStepDetailResponse(detail.getText(), detail.getStart());
            }
        }

        /**
         * 엔티티로부터 단계 응답 DTO 생성
         *
         * @param step 단계 엔티티
         * @return 단계 응답 DTO
         */
        public static RecipeStepResponse from(RecipeStep step) {
            return new RecipeStepResponse(
                    step.getId(),
                    step.getStepOrder(),
                    step.getSubtitle(),
                    step.getDetails().stream()
                            .map(RecipeStepDetailResponse::from)
                            .toList(),
                    step.getStart());
        }
    }
}
