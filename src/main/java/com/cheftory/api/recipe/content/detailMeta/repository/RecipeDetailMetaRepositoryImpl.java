package com.cheftory.api.recipe.content.detailMeta.repository;

import com.cheftory.api._common.aspect.DbThrottled;
import com.cheftory.api.recipe.content.detailMeta.entity.RecipeDetailMeta;
import com.cheftory.api.recipe.content.detailMeta.exception.RecipeDetailMetaErrorCode;
import com.cheftory.api.recipe.content.detailMeta.exception.RecipeDetailMetaException;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

/**
 * 레시피 상세 메타 정보 Repository 구현체
 */
@Repository
@RequiredArgsConstructor
@Slf4j
public class RecipeDetailMetaRepositoryImpl implements RecipeDetailMetaRepository {

    private final RecipeDetailMetaJpaRepository repository;

    @Override
    public RecipeDetailMeta get(UUID recipeId) throws RecipeDetailMetaException {
        return repository
                .findByRecipeId(recipeId)
                .orElseThrow(() -> new RecipeDetailMetaException(RecipeDetailMetaErrorCode.DETAIL_META_NOT_FOUND));
    }

    @Override
    public List<RecipeDetailMeta> gets(List<UUID> recipeIds) {
        return repository.findAllByRecipeIdIn(recipeIds);
    }

    @DbThrottled
    @Override
    public void create(RecipeDetailMeta recipeDetailMeta) {
        repository.save(recipeDetailMeta);
    }
}
