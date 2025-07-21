package com.cheftory.api.recipeviewstate.dto;

import com.cheftory.api.recipeviewstate.entity.RecipeViewState;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder(access = AccessLevel.PRIVATE)
@Getter
public class SimpleAccessInfo {
  private UUID id;
  private LocalDateTime viewedAt;
  private LocalDateTime createdAt;
  private UUID userId;
  private UUID recipeId;

  public static SimpleAccessInfo from(RecipeViewState recipeViewState) {
    return SimpleAccessInfo.builder()
        .id(recipeViewState.getId())
        .viewedAt(recipeViewState.getViewedAt())
        .createdAt(recipeViewState.getCreatedAt())
        .userId(recipeViewState.getUserId())
        .recipeId(recipeViewState.getRecipeId())
        .build();
  }
}
