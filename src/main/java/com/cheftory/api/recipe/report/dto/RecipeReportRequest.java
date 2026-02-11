package com.cheftory.api.recipe.report.dto;

import com.cheftory.api.recipe.report.entity.RecipeReportReason;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * 레시피 신고 요청 DTO
 */
public record RecipeReportRequest(
        @JsonProperty("reason") @NotNull RecipeReportReason reason,
        @JsonProperty("description") @Size(max = 500) String description) {}
