package com.cheftory.api.recipe.model;

import com.cheftory.api.recipe.entity.Recipe;
import com.cheftory.api.recipe.viewstatus.RecipeViewStatus;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder(access = AccessLevel.PRIVATE)
@Getter
public class RecipeHistoryOverview {
  private RecipeOverview recipeOverview;
  private RecipeViewStatusInfo recipeViewStatusInfo;

  public static RecipeHistoryOverview of(
      Recipe recipe,
      RecipeViewStatus recipeViewStatus
  ) {
    return RecipeHistoryOverview.builder()
        .recipeOverview(RecipeOverview.from(recipe))
        .recipeViewStatusInfo(RecipeViewStatusInfo.of(recipeViewStatus))
        .build();
  }
}
