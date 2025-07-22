package com.cheftory.api.recipe.model;

import com.cheftory.api.recipe.viewstatus.RecipeViewStatusInfo;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder(access = AccessLevel.PRIVATE)
@Getter
public class RecentRecipeOverview {
  private RecipeOverview recipeOverview;
  private RecipeViewStatusInfo recipeViewStatusInfo;

  public static RecentRecipeOverview of(
      RecipeOverview recipeOverview,
      RecipeViewStatusInfo recipeViewStatusInfo
  ) {
    return RecentRecipeOverview.builder()
        .recipeOverview(recipeOverview)
        .recipeViewStatusInfo(recipeViewStatusInfo)
        .build();
  }
}
