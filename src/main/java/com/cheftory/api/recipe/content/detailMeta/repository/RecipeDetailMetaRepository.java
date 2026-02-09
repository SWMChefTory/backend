package com.cheftory.api.recipe.content.detailMeta.repository;

import com.cheftory.api._common.aspect.DbThrottled;
import com.cheftory.api.recipe.content.detailMeta.entity.RecipeDetailMeta;
import com.cheftory.api.recipe.content.detailMeta.exception.RecipeDetailMetaException;
import java.util.List;
import java.util.UUID;

/**
 * 레시피 상세 메타 정보 Repository 인터페이스
 */
public interface RecipeDetailMetaRepository {
    /**
     * 레시피 ID로 상세 메타 정보 조회
     *
     * @param recipeId 레시피 ID
     * @return 조회된 상세 메타 정보
     * @throws RecipeDetailMetaException 정보를 찾을 수 없을 때 RECIPE_DETAIL_META_NOT_FOUND
     */
    RecipeDetailMeta get(UUID recipeId) throws RecipeDetailMetaException;

    /**
     * 여러 레시피 ID로 상세 메타 정보 목록 조회
     *
     * @param recipeIds 레시피 ID 목록
     * @return 상세 메타 정보 목록
     */
    List<RecipeDetailMeta> gets(List<UUID> recipeIds);

    /**
     * 레시피 상세 메타 정보 생성
     *
     * @param recipeDetailMeta 생성할 상세 메타 정보 엔티티
     */
    @DbThrottled
    void create(RecipeDetailMeta recipeDetailMeta);
}
