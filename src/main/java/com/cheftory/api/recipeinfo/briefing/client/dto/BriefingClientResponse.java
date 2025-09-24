package com.cheftory.api.recipeinfo.briefing.client.dto;

import com.cheftory.api._common.Clock;
import com.cheftory.api.recipeinfo.briefing.RecipeBriefing;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import java.util.UUID;

public record BriefingClientResponse(@JsonProperty("briefings") @NotNull List<String> briefings) {
  public List<RecipeBriefing> toRecipeBriefing(UUID recipeId, Clock clock) {
    return briefings.stream()
        .map(briefing -> RecipeBriefing.create(recipeId, briefing, clock))
        .toList();
  }
}
