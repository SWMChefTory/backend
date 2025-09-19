package com.cheftory.api.recipeinfo.model;

import com.cheftory.api.recipeinfo.detailMeta.RecipeDetailMeta;
import com.cheftory.api.recipeinfo.recipe.entity.Recipe;
import com.cheftory.api.recipeinfo.tag.RecipeTag;
import com.cheftory.api.recipeinfo.youtubemeta.RecipeYoutubeMeta;
import java.util.List;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder(access = AccessLevel.PRIVATE)
@Getter
public class RecipeOverview {
  private Recipe recipe;
  private RecipeYoutubeMeta youtubeMeta;
  private RecipeDetailMeta detailMeta;
  private List<RecipeTag> tags;

  public static RecipeOverview of(
      Recipe recipe,
      RecipeYoutubeMeta youtubeMeta,
      RecipeDetailMeta detailMeta,
      List<RecipeTag> tags) {
    return RecipeOverview.builder()
        .recipe(recipe)
        .youtubeMeta(youtubeMeta)
        .detailMeta(detailMeta)
        .tags(tags)
        .build();
  }
}
