package com.cheftory.api.recipe.category.repository;

import com.cheftory.api.recipe.category.entity.RecipeCategory;
import com.cheftory.api.recipe.category.exception.RecipeCategoryException;
import java.util.List;
import java.util.UUID;

/**
 * 레시피 카테고리 Repository 인터페이스
 */
public interface RecipeCategoryRepository {
    /**
     * 레시피 카테고리 생성
     *
     * @param recipeCategory 생성할 레시피 카테고리 엔티티
     * @return 생성된 레시피 카테고리 ID
     */
    UUID create(RecipeCategory recipeCategory);

    /**
     * 레시피 카테고리 삭제
     *
     * @param userId 유저 ID
     * @param recipeCategoryId 레시피 카테고리 ID
     * @throws RecipeCategoryException 카테고리를 찾을 수 없을 때 RECIPE_CATEGORY_NOT_FOUND
     */
    void delete(UUID userId, UUID recipeCategoryId) throws RecipeCategoryException;

    /**
     * 유저의 레시피 카테고리 목록 조회
     *
     * @param userId 유저 ID
     * @return 레시피 카테고리 목록
     */
    List<RecipeCategory> gets(UUID userId);

    /**
     * 레시피 카테고리 존재 여부 확인
     *
     * @param recipeCategoryId 레시피 카테고리 ID
     * @return 존재 여부
     */
    boolean exists(UUID recipeCategoryId);
}
