package com.cheftory.api.recipe.dto;

import java.util.List;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
public class RecipeCategoryCounts {
  private final Integer uncategorizedCount;
  private final List<RecipeCategoryCount> categorizedCounts;
  private final Integer totalCount;

  public static RecipeCategoryCounts of(
      Integer uncategorizedCount, List<RecipeCategoryCount> categorizedCounts) {

    int total =
        uncategorizedCount
            + categorizedCounts.stream().mapToInt(RecipeCategoryCount::getRecipeCount).sum();

    return new RecipeCategoryCounts(uncategorizedCount, categorizedCounts, total);
  }
}
