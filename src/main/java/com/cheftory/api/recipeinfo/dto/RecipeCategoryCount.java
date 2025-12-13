package com.cheftory.api.recipeinfo.model;

import com.cheftory.api.recipeinfo.category.RecipeCategory;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder(access = AccessLevel.PRIVATE)
@Getter
public class RecipeCategoryCount {

  private RecipeCategory category;
  private Integer recipeCount;

  public static RecipeCategoryCount of(RecipeCategory category, Integer count) {
    return RecipeCategoryCount.builder().category(category).recipeCount(count).build();
  }
}
