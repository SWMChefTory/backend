package com.cheftory.api.recipe.content.tag.repository;

import com.cheftory.api.recipe.content.tag.entity.RecipeTag;
import java.util.List;
import java.util.UUID;

/**
 * 레시피 태그 Repository 인터페이스
 */
public interface RecipeTagRepository {
    /**
     * 레시피 ID로 태그 목록 조회
     *
     * @param recipeId 레시피 ID
     * @return 태그 목록
     */
    List<RecipeTag> finds(UUID recipeId);

    /**
     * 여러 레시피 ID로 태그 목록 조회
     *
     * @param recipeIds 레시피 ID 목록
     * @return 태그 목록
     */
    List<RecipeTag> finds(List<UUID> recipeIds);

    /**
     * 레시피 태그 목록 일괄 저장
     *
     * @param recipeTags 저장할 태그 목록
     */
    void create(List<RecipeTag> recipeTags);
}
