package com.cheftory.api.recipeinfo.model;

import com.cheftory.api.recipeinfo.briefing.RecipeBriefing;
import com.cheftory.api.recipeinfo.detailMeta.RecipeDetailMeta;
import com.cheftory.api.recipeinfo.history.RecipeHistory;
import com.cheftory.api.recipeinfo.ingredient.RecipeIngredient;
import com.cheftory.api.recipeinfo.progress.RecipeProgress;
import com.cheftory.api.recipeinfo.recipe.entity.Recipe;
import com.cheftory.api.recipeinfo.step.entity.RecipeStep;
import com.cheftory.api.recipeinfo.tag.RecipeTag;
import com.cheftory.api.recipeinfo.youtubemeta.RecipeYoutubeMeta;
import jakarta.annotation.Nullable;
import java.util.List;
import lombok.*;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder(access = AccessLevel.PRIVATE)
@Getter
public class FullRecipe {
  private List<RecipeIngredient> recipeIngredients;
  private List<RecipeTag> recipeTags;
  @Nullable private RecipeDetailMeta recipeDetailMeta;
  private List<RecipeStep> recipeSteps;
  private List<RecipeProgress> recipeProgresses;
  private RecipeHistory recipeHistory;
  private RecipeYoutubeMeta recipeYoutubeMeta;
  private Recipe recipe;
  private List<RecipeBriefing> recipeBriefings;

  public static FullRecipe of(
      List<RecipeStep> recipeSteps,
      List<RecipeIngredient> recipeIngredients,
      @Nullable RecipeDetailMeta recipeDetailMeta,
      List<RecipeProgress> recipeProgresses,
      List<RecipeTag> recipeTags,
      RecipeYoutubeMeta recipeYoutubeMeta,
      RecipeHistory recipeHistory,
      Recipe recipe,
      List<RecipeBriefing> recipeBriefings) {
    return FullRecipe.builder()
        .recipeYoutubeMeta(recipeYoutubeMeta)
        .recipeIngredients(recipeIngredients)
        .recipeTags(recipeTags)
        .recipeDetailMeta(recipeDetailMeta)
        .recipeSteps(recipeSteps)
        .recipeProgresses(recipeProgresses)
        .recipeHistory(recipeHistory)
        .recipe(recipe)
        .recipeBriefings(recipeBriefings)
        .build();
  }
}
