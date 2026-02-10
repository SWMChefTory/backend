package com.cheftory.api.recipe.content.briefing.respotiory;

import com.cheftory.api._common.aspect.DbThrottled;
import com.cheftory.api.recipe.content.briefing.entity.RecipeBriefing;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

/**
 * 레시피 브리핑 Repository 구현체
 */
@Repository
@RequiredArgsConstructor
public class RecipeBriefingRepositoryImpl implements RecipeBriefingRepository {

    private final RecipeBriefingJpaRepository repository;

    @Override
    public List<RecipeBriefing> findAllByRecipeId(UUID recipeId) {
        return repository.findAllByRecipeId(recipeId);
    }

    @DbThrottled
    @Override
    public void saveAll(List<RecipeBriefing> recipeBriefings) {
        repository.saveAll(recipeBriefings);
    }
}
