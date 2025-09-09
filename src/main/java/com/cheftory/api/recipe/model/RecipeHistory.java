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
public class RecipeHistory {
  private Recipe recipe;
  private RecipeViewStatus recipeViewStatus;

  public static RecipeHistory of(
      Recipe recipe,
      RecipeViewStatus recipeViewStatus
  ) {
    return RecipeHistory.builder()
        .recipe(recipe)
        .recipeViewStatus(recipeViewStatus)
        .build();
  }
}
