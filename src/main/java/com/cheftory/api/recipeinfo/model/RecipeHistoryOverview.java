package com.cheftory.api.recipeinfo.model;

import com.cheftory.api.recipeinfo.detailMeta.RecipeDetailMeta;
import com.cheftory.api.recipeinfo.history.RecipeHistory;
import com.cheftory.api.recipeinfo.recipe.entity.Recipe;
import com.cheftory.api.recipeinfo.tag.RecipeTag;
import com.cheftory.api.recipeinfo.youtubemeta.RecipeYoutubeMeta;
import java.util.List;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import org.springframework.lang.Nullable;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder(access = AccessLevel.PRIVATE)
@Getter
public class RecipeHistoryOverview {
  private Recipe recipe;
  private RecipeHistory recipeHistory;
  private RecipeYoutubeMeta youtubeMeta;
  private RecipeDetailMeta detailMeta;
  private List<RecipeTag> tags;

  public static RecipeHistoryOverview of(
      Recipe recipe,
      RecipeHistory recipeHistory,
      RecipeYoutubeMeta youtubeMeta,
      @Nullable RecipeDetailMeta detailMeta,
      @Nullable List<RecipeTag> tags) {
    return RecipeHistoryOverview.builder()
        .recipe(recipe)
        .recipeHistory(recipeHistory)
        .youtubeMeta(youtubeMeta)
        .detailMeta(detailMeta)
        .tags(tags)
        .build();
  }
}
