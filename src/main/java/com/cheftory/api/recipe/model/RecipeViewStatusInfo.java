package com.cheftory.api.recipe.model;

import com.cheftory.api.recipe.viewstatus.RecipeViewStatus;
import jakarta.annotation.Nullable;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder(access = AccessLevel.PRIVATE)
@Getter
public class RecipeViewStatusInfo {

  private UUID id;
  private LocalDateTime viewedAt;
  private Integer lastPlaySeconds;
  private LocalDateTime createdAt;
  private UUID recipeId;
  private UUID userId;
  @Nullable
  private UUID categoryId;

  public static RecipeViewStatusInfo of(
      RecipeViewStatus recipeViewStatus
  ) {
    return RecipeViewStatusInfo.builder()
        .id(recipeViewStatus.getId())
        .viewedAt(recipeViewStatus.getViewedAt())
        .lastPlaySeconds(recipeViewStatus.getLastPlaySeconds())
        .createdAt(recipeViewStatus.getCreatedAt())
        .recipeId(recipeViewStatus.getRecipeId())
        .userId(recipeViewStatus.getUserId())
        .categoryId(recipeViewStatus.getRecipeCategoryId())
        .build();
  }
}
