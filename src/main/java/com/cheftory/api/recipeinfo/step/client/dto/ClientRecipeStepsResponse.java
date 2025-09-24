package com.cheftory.api.recipeinfo.step.client.dto;

import com.cheftory.api._common.Clock;
import com.cheftory.api.recipeinfo.step.entity.RecipeStep;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import java.util.UUID;
import java.util.stream.IntStream;

public record ClientRecipeStepsResponse(@JsonProperty("steps") @NotNull List<Step> steps) {
  public record Step(
      @JsonProperty("subtitle") @NotNull String subtitle,
      @JsonProperty("start") Double start,
      @JsonProperty("descriptions") @NotNull List<Description> descriptions) {
    private record Description(
        @JsonProperty("text") @NotNull String text, @JsonProperty("start") @NotNull Double start) {
      private RecipeStep.Detail toRecipeStepDetail() {
        return RecipeStep.Detail.builder().text(text).start(start).build();
      }
    }
  }

  public List<RecipeStep> toRecipeSteps(UUID recipeId, Clock clock) {
    return IntStream.range(0, steps.size())
        .mapToObj(
            i -> {
              Step step = steps.get(i);
              List<RecipeStep.Detail> details =
                  step.descriptions().stream().map(Step.Description::toRecipeStepDetail).toList();
              return RecipeStep.create(
                  i + 1, step.subtitle(), details, step.start(), recipeId, clock);
            })
        .toList();
  }
}
