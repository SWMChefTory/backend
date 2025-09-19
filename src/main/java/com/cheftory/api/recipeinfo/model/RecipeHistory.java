package com.cheftory.api.recipeinfo.model;

import com.cheftory.api.recipeinfo.recipe.entity.Recipe;
import com.cheftory.api.recipeinfo.viewstatus.RecipeViewStatus;
import com.cheftory.api.recipeinfo.youtubemeta.RecipeYoutubeMeta;
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
  private RecipeYoutubeMeta youtubeMeta;

  public static RecipeHistory of(
      Recipe recipe, RecipeViewStatus recipeViewStatus, RecipeYoutubeMeta youtubeMeta) {
    return RecipeHistory.builder()
        .recipe(recipe)
        .recipeViewStatus(recipeViewStatus)
        .youtubeMeta(youtubeMeta)
        .build();
  }
}
