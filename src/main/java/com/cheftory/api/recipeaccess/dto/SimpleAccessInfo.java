package com.cheftory.api.recipeaccess.dto;

import com.cheftory.api.recipe.dto.RecipeOverview;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder(access = AccessLevel.PRIVATE)
@Getter
public class SimpleAccessInfo {
  private LocalDateTime viewedAt;
  private LocalDateTime createdAt;
  private RecipeOverview recipeOverview;
  public static SimpleAccessInfo of(
      com.cheftory.api.recipeviewstate.dto.SimpleAccessInfo simpleAccessInfo,
      RecipeOverview recipeOverview
  ) {
    return com.cheftory.api.recipeaccess.dto.SimpleAccessInfo.builder()
        .viewedAt(simpleAccessInfo.getViewedAt())
        .createdAt(simpleAccessInfo.getCreatedAt())
        .recipeOverview(recipeOverview)
        .build();
  }
}
