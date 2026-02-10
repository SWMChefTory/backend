package com.cheftory.api.recipe.content.tag.repository;

import com.cheftory.api._common.aspect.DbThrottled;
import com.cheftory.api.recipe.content.tag.entity.RecipeTag;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

/**
 * 레시피 태그 Repository 구현체
 */
@Repository
@RequiredArgsConstructor
public class RecipeTagRepositoryImpl implements RecipeTagRepository {

    private final RecipeTagJpaRepository repository;

    /**
     * 레시피 ID로 태그 목록 조회
     *
     * @param recipeId 레시피 ID
     * @return 태그 목록
     */
    @Override
    public List<RecipeTag> finds(UUID recipeId) {
        return repository.findAllByRecipeId(recipeId);
    }

    /**
     * 여러 레시피 ID로 태그 목록 조회
     *
     * @param recipeIds 레시피 ID 목록
     * @return 태그 목록
     */
    @Override
    public List<RecipeTag> finds(List<UUID> recipeIds) {
        return repository.findAllByRecipeIdIn(recipeIds);
    }

    /**
     * 레시피 태그 목록 일괄 저장
     *
     * <p>데이터베이스 부하를 고려하여 스로틀링이 적용되어 있습니다.</p>
     *
     * @param recipeTags 저장할 태그 목록
     */
    @DbThrottled
    @Override
    public void create(List<RecipeTag> recipeTags) {
        repository.saveAll(recipeTags);
    }
}
