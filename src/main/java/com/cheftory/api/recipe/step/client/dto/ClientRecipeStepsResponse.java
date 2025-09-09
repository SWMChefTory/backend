package com.cheftory.api.recipe.step.client.dto;

import com.cheftory.api.recipe.step.entity.RecipeStep;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import java.util.UUID;
import java.util.stream.IntStream;

public record ClientRecipeStepsResponse(
    @JsonProperty("steps")
    @NotNull
    List<Step> steps
) {
    private record Step(
        @JsonProperty("subtitle")
        @NotNull
        String subtitle,
        @JsonProperty("start")
        Double start,
        @NotNull
        @JsonProperty("descriptions")
        List<Description> descriptions
    ) {
        private record Description(
            @JsonProperty("text")
            @NotNull
            String text,
            @JsonProperty("start")
            @NotNull
            Double start
        ) {
            public RecipeStep.Detail toRecipeStepDetail() {
                return RecipeStep.Detail.builder().text(text).start(start).build();
            }
        }
    }

    public List<RecipeStep> toRecipeSteps(UUID recipeId) {
        return IntStream.range(0, steps.size())
            .mapToObj(i -> {
                Step step = steps.get(i);
                List<RecipeStep.Detail> details = step.descriptions().stream()
                    .map(Step.Description::toRecipeStepDetail)
                    .toList();
                return RecipeStep.from(i + 1, step.subtitle(), details, step.start(), recipeId);
            })
            .toList();
    }
}