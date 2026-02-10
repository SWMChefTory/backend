package com.cheftory.api.recipe.content.step.repository;

import com.cheftory.api._common.aspect.DbThrottled;
import com.cheftory.api.recipe.content.step.entity.RecipeStep;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;

/**
 * 레시피 단계 Repository 구현체
 */
@Repository
@RequiredArgsConstructor
public class RecipeStepRepositoryImpl implements RecipeStepRepository {

    private final RecipeStepJpaRepository repository;

    @Override
    public List<RecipeStep> finds(UUID recipeId, Sort sort) {
        return repository.findAllByRecipeId(recipeId, sort);
    }

    @DbThrottled
    @Override
    public List<UUID> create(List<RecipeStep> recipeSteps) {
        return repository.saveAll(recipeSteps).stream().map(RecipeStep::getId).toList();
    }
}
