package com.cheftory.api.recipe.content.scene.client.dto;

import com.cheftory.api.recipe.content.step.entity.RecipeStep;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import java.util.UUID;

/**
 * 외부 scene 생성 API 요청 DTO.
 */
public record ClientRecipeScenesRequest(
        @JsonProperty("file_uri") String fileUri,
        @JsonProperty("mime_type") String mimeType,
        @JsonProperty("steps") List<Step> steps) {

    public static ClientRecipeScenesRequest from(String fileUri, String mimeType, List<RecipeStep> recipeSteps) {
        return new ClientRecipeScenesRequest(
                fileUri, mimeType, recipeSteps.stream().map(Step::from).toList());
    }

    public record Step(
            @JsonProperty("step_id") UUID stepId,
            @JsonProperty("subtitle") String subtitle,
            @JsonProperty("start") Double start,
            @JsonProperty("descriptions") List<Description> descriptions) {

        public static Step from(RecipeStep recipeStep) {
            List<RecipeStep.Detail> details = recipeStep.getDetails() == null ? List.of() : recipeStep.getDetails();
            Double start = recipeStep.getStart();
            if (start == null && !details.isEmpty()) {
                start = details.get(0).getStart();
            }
            if (start == null) {
                start = 0.0;
            }

            return new Step(
                    recipeStep.getId(),
                    recipeStep.getSubtitle(),
                    start,
                    details.stream().map(Description::from).toList());
        }

        public record Description(
                @JsonProperty("text") String text,
                @JsonProperty("start") Double start) {

            public static Description from(RecipeStep.Detail detail) {
                return new Description(detail.getText(), detail.getStart());
            }
        }
    }
}
