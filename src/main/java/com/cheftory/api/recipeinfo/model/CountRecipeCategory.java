package com.cheftory.api.recipeinfo.model;

import com.cheftory.api.recipeinfo.category.RecipeCategory;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder(access = AccessLevel.PRIVATE)
@Getter
public class CountRecipeCategory {

  private RecipeCategory category;
  private Integer count;

  public static CountRecipeCategory of(RecipeCategory category, Integer count) {
    return CountRecipeCategory.builder().category(category).count(count).build();
  }
}
