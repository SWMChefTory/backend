package com.cheftory.api.recipeinfo.history;

import java.util.UUID;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder(access = AccessLevel.PRIVATE)
@Getter
@NoArgsConstructor
public class RecipeHistoryCategorizedCount {
  private UUID categoryId;
  private Integer count;

  public static RecipeHistoryCategorizedCount of(UUID categoryId, Integer count) {
    return new RecipeHistoryCategorizedCount(categoryId, count);
  }
}
