package com.cheftory.api.recipe.model;

import com.cheftory.api.recipe.category.RecipeCategory;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder(access = AccessLevel.PRIVATE)
@Getter
public class RecipeCategoryCount {

  private RecipeCategory category;
  private Integer count;

  public static RecipeCategoryCount of(RecipeCategory category, Integer count) {
    return RecipeCategoryCount.builder()
        .category(category)
        .count(count)
        .build();
  }
}
