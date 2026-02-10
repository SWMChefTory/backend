package com.cheftory.api.recipe.dto;

import com.cheftory.api.recipe.category.entity.RecipeCategory;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 레시피 카테고리별 개수 정보
 */
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
public class RecipeCategoryCount {

    /**
     * 레시피 카테고리
     */
    private RecipeCategory recipeCategory;
    /**
     * 레시피 개수
     */
    private Integer recipeCount;

    /**
     * RecipeCategoryCount 생성 팩토리 메서드
     *
     * @param recipeCategory 레시피 카테고리
     * @param count 레시피 개수
     * @return 레시피 카테고리 개수 객체
     */
    public static RecipeCategoryCount of(RecipeCategory recipeCategory, Integer count) {
        return new RecipeCategoryCount(recipeCategory, count);
    }
}
