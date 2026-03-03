package com.cheftory.api.tracking.repository;

import com.cheftory.api.tracking.entity.RecipeImpression;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

/**
 * 레시피 노출 Repository 구현체.
 */
@Repository
@RequiredArgsConstructor
public class RecipeImpressionRepositoryImpl implements RecipeImpressionRepository {

    private final RecipeImpressionJpaRepository repository;

    @Override
    public void saveAll(List<RecipeImpression> recipeImpressions) {
        repository.saveAll(recipeImpressions);
    }
}
