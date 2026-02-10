package com.cheftory.api.recipe.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import java.util.UUID;

/**
 * 레시피 카테고리별 개수 응답 DTO
 *
 * @param uncategorizedCount 분류되지 않은 레시피 개수
 * @param categories 카테고리별 개수 목록
 * @param totalCount 전체 레시피 개수
 */
public record RecipeCategoryCountsResponse(
        @JsonProperty("unCategorized_count") Integer uncategorizedCount,
        @JsonProperty("categories") List<CategoryCount> categories,
        @JsonProperty("total_count") Integer totalCount) {

    /**
     * RecipeCategoryCounts로부터 응답 DTO 생성
     *
     * @param counts 레시피 카테고리별 개수 집계 객체
     * @return 레시피 카테고리별 개수 응답 DTO
     */
    public static RecipeCategoryCountsResponse from(RecipeCategoryCounts counts) {
        List<CategoryCount> categoryResponses =
                counts.getCategorizedCounts().stream().map(CategoryCount::from).toList();
        return new RecipeCategoryCountsResponse(
                counts.getUncategorizedCount(), categoryResponses, counts.getTotalCount());
    }

    /**
     * 카테고리별 개수 응답 레코드
     *
     * @param categoryId 카테고리 ID
     * @param name 카테고리 이름
     * @param count 레시피 개수
     */
    public record CategoryCount(
            @JsonProperty("category_id") UUID categoryId,
            @JsonProperty("name") String name,
            @JsonProperty("count") Integer count) {

        /**
         * RecipeCategoryCount로부터 응답 DTO 생성
         *
         * @param categoryCount 레시피 카테고리 개수 객체
         * @return 카테고리별 개수 응답 DTO
         */
        public static CategoryCount from(RecipeCategoryCount categoryCount) {
            return new CategoryCount(
                    categoryCount.getRecipeCategory().getId(),
                    categoryCount.getRecipeCategory().getName(),
                    categoryCount.getRecipeCount());
        }
    }
}
