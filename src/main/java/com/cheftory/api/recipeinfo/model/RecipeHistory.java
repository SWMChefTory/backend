package com.cheftory.api.recipeinfo.model;

import com.cheftory.api.recipeinfo.detailMeta.RecipeDetailMeta;
import com.cheftory.api.recipeinfo.recipe.entity.Recipe;
import com.cheftory.api.recipeinfo.tag.RecipeTag;
import com.cheftory.api.recipeinfo.viewstatus.RecipeViewStatus;
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
public class RecipeHistory {
  private Recipe recipe;
  private RecipeViewStatus recipeViewStatus;
  private RecipeYoutubeMeta youtubeMeta;
  private RecipeDetailMeta detailMeta;
  private List<RecipeTag> tags;

  public static RecipeHistory of(
      Recipe recipe,
      RecipeViewStatus recipeViewStatus,
      RecipeYoutubeMeta youtubeMeta,
      @Nullable RecipeDetailMeta detailMeta,
      @Nullable List<RecipeTag> tags) {
    return RecipeHistory.builder()
        .recipe(recipe)
        .recipeViewStatus(recipeViewStatus)
        .youtubeMeta(youtubeMeta)
        .detailMeta(detailMeta)
        .tags(tags)
        .build();
  }
}
