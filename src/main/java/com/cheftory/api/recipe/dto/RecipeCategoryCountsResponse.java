package com.cheftory.api.recipe.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import java.util.UUID;

public record RecipeCategoryCountsResponse(
        @JsonProperty("unCategorized_count") Integer uncategorizedCount,
        @JsonProperty("categories") List<CategoryCount> categories,
        @JsonProperty("total_count") Integer totalCount) {

    public static RecipeCategoryCountsResponse from(RecipeCategoryCounts counts) {
        List<CategoryCount> categoryResponses =
                counts.getCategorizedCounts().stream().map(CategoryCount::from).toList();
        return new RecipeCategoryCountsResponse(
                counts.getUncategorizedCount(), categoryResponses, counts.getTotalCount());
    }

    public record CategoryCount(
            @JsonProperty("category_id") UUID categoryId,
            @JsonProperty("name") String name,
            @JsonProperty("count") Integer count) {

        public static CategoryCount from(RecipeCategoryCount categoryCount) {
            return new CategoryCount(
                    categoryCount.getRecipeCategory().getId(),
                    categoryCount.getRecipeCategory().getName(),
                    categoryCount.getRecipeCount());
        }
    }
}
