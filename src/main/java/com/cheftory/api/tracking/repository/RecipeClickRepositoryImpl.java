package com.cheftory.api.tracking.repository;

import com.cheftory.api.tracking.entity.RecipeClick;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

/**
 * 레시피 클릭 Repository 구현체.
 */
@Repository
@RequiredArgsConstructor
public class RecipeClickRepositoryImpl implements RecipeClickRepository {

    private final RecipeClickJpaRepository repository;

    @Override
    public void save(RecipeClick recipeClick) {
        repository.save(recipeClick);
    }
}
