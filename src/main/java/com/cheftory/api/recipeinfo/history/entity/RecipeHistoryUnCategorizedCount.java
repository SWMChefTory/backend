package com.cheftory.api.recipeinfo.history.entity;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder(access = AccessLevel.PRIVATE)
@Getter
@NoArgsConstructor
public class RecipeHistoryUnCategorizedCount {
  private Integer count;

  public static RecipeHistoryUnCategorizedCount of(Integer count) {
    return new RecipeHistoryUnCategorizedCount(count);
  }
}
