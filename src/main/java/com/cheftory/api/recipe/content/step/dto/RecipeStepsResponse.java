package com.cheftory.api.recipe.content.step.dto;

import com.cheftory.api.recipe.content.step.entity.RecipeStep;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import java.util.UUID;

public record RecipeStepsResponse(@JsonProperty("steps") List<RecipeStepResponse> steps) {
    public static RecipeStepsResponse from(List<RecipeStep> recipeSteps) {
        List<RecipeStepResponse> stepResponses =
                recipeSteps.stream().map(RecipeStepResponse::from).toList();
        return new RecipeStepsResponse(stepResponses);
    }

    public record RecipeStepResponse(
            @JsonProperty("id") UUID id,
            @JsonProperty("step_order") Integer stepOrder,
            @JsonProperty("subtitle") String subtitle,
            @JsonProperty("details") List<RecipeStepDetailResponse> details,
            @JsonProperty("start") Double start) {
        public record RecipeStepDetailResponse(@JsonProperty("text") String text, @JsonProperty("start") Double start) {
            public static RecipeStepDetailResponse from(RecipeStep.Detail detail) {
                return new RecipeStepDetailResponse(detail.getText(), detail.getStart());
            }
        }

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
