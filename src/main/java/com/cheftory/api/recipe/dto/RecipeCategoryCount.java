package com.cheftory.api.recipe.dto;

import com.cheftory.api.recipe.category.entity.RecipeCategory;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
public class RecipeCategoryCount {

  private RecipeCategory recipeCategory;
  private Integer recipeCount;

  public static RecipeCategoryCount of(RecipeCategory recipeCategory, Integer count) {
    return new RecipeCategoryCount(recipeCategory, count);
  }
}
