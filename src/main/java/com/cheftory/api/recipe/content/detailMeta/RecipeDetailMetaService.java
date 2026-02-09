package com.cheftory.api.recipe.content.detailMeta;

import com.cheftory.api._common.Clock;
import com.cheftory.api.recipe.content.detailMeta.entity.RecipeDetailMeta;
import com.cheftory.api.recipe.content.detailMeta.exception.RecipeDetailMetaException;
import com.cheftory.api.recipe.content.detailMeta.repository.RecipeDetailMetaRepository;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 레시피 상세 메타 정보 도메인의 비즈니스 로직을 처리하는 서비스
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class RecipeDetailMetaService {
    private final RecipeDetailMetaRepository recipeDetailMetaRepository;
    private final Clock clock;

    /**
     * 레시피 상세 메타 정보 조회
     *
     * @param recipeId 레시피 ID
     * @return 레시피 상세 메타 정보 엔티티
     * @throws RecipeDetailMetaException 정보를 찾을 수 없을 때 RECIPE_DETAIL_META_NOT_FOUND
     */
    public RecipeDetailMeta get(UUID recipeId) throws RecipeDetailMetaException {
        return recipeDetailMetaRepository.get(recipeId);
    }

    /**
     * 여러 레시피의 상세 메타 정보 목록 조회
     *
     * @param recipeIds 레시피 ID 목록
     * @return 레시피 상세 메타 정보 목록
     */
    public List<RecipeDetailMeta> getIn(List<UUID> recipeIds) {
        return recipeDetailMetaRepository.gets(recipeIds);
    }

    /**
     * 레시피 상세 메타 정보 생성
     *
     * @param recipeId 레시피 ID
     * @param cookTime 조리 시간 (분)
     * @param servings 인분
     * @param description 레시피 설명
     */
    public void create(UUID recipeId, Integer cookTime, Integer servings, String description) {
        RecipeDetailMeta recipeDetailMeta = RecipeDetailMeta.create(cookTime, servings, description, clock, recipeId);
        recipeDetailMetaRepository.create(recipeDetailMeta);
    }
}
