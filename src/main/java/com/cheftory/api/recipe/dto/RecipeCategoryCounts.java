package com.cheftory.api.recipe.dto;

import java.util.List;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 레시피 카테고리별 개수 집계 정보
 */
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
public class RecipeCategoryCounts {
    /**
     * 분류되지 않은 레시피 개수
     */
    private final Integer uncategorizedCount;
    /**
     * 카테고리별 레시피 개수 목록
     */
    private final List<RecipeCategoryCount> categorizedCounts;
    /**
     * 전체 레시피 개수
     */
    private final Integer totalCount;

    /**
     * RecipeCategoryCounts 생성 팩토리 메서드
     *
     * @param uncategorizedCount 분류되지 않은 레시피 개수
     * @param categorizedCounts 카테고리별 레시피 개수 목록
     * @return 레시피 카테고리별 개수 집계 객체
     */
    public static RecipeCategoryCounts of(Integer uncategorizedCount, List<RecipeCategoryCount> categorizedCounts) {

        int total = uncategorizedCount
                + categorizedCounts.stream()
                        .mapToInt(RecipeCategoryCount::getRecipeCount)
                        .sum();

        return new RecipeCategoryCounts(uncategorizedCount, categorizedCounts, total);
    }
}
