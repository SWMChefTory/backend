package com.cheftory.api.recipeinfo.dto;

import java.util.List;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder(access = AccessLevel.PRIVATE)
@Getter
public class RecipeCategoryCounts {
  private final Integer uncategorizedCount;
  private final List<RecipeCategoryCount> categorizedCounts;
  private final Integer totalCount;

  public static RecipeCategoryCounts of(
      Integer uncategorizedCount, List<RecipeCategoryCount> categorizedCounts) {
    Integer total =
        uncategorizedCount
            + categorizedCounts.stream().mapToInt(RecipeCategoryCount::getRecipeCount).sum();

    return RecipeCategoryCounts.builder()
        .uncategorizedCount(uncategorizedCount)
        .categorizedCounts(categorizedCounts)
        .totalCount(total)
        .build();
  }
}
