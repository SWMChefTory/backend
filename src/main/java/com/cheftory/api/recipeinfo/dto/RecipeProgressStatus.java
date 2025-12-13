package com.cheftory.api.recipeinfo.dto;

import com.cheftory.api.recipeinfo.progress.entity.RecipeProgress;
import com.cheftory.api.recipeinfo.recipe.entity.Recipe;
import java.util.List;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder(access = AccessLevel.PRIVATE)
@Getter
public class RecipeProgressStatus {

  private List<RecipeProgress> progresses;
  private Recipe recipe;

  public static RecipeProgressStatus of(Recipe recipe, List<RecipeProgress> progresses) {
    return new RecipeProgressStatus(progresses, recipe);
  }
}
