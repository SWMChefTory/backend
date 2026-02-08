package com.cheftory.api.recipe.category.repository;

import com.cheftory.api.recipe.category.entity.RecipeCategory;
import com.cheftory.api.recipe.category.entity.RecipeCategoryStatus;
import com.cheftory.api.recipe.category.exception.RecipeCategoryErrorCode;
import com.cheftory.api.recipe.category.exception.RecipeCategoryException;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
@Slf4j
public class RecipeCategoryRepositoryImpl implements RecipeCategoryRepository {

    private final RecipeCategoryJpaRepository repository;

    @Override
    public UUID create(RecipeCategory recipeCategory) {
        return repository.save(recipeCategory).getId();
    }

    @Override
    public void delete(UUID userId, UUID recipeCategoryId) throws RecipeCategoryException {
        RecipeCategory recipeCategory = repository
                .findByIdAndUserIdAndStatus(recipeCategoryId, userId, RecipeCategoryStatus.ACTIVE)
                .orElseThrow(() -> new RecipeCategoryException(RecipeCategoryErrorCode.RECIPE_CATEGORY_NOT_FOUND));
        recipeCategory.delete();
        repository.save(recipeCategory);
    }

    @Override
    public List<RecipeCategory> gets(UUID userId) {
        return repository.findAllByUserIdAndStatus(userId, RecipeCategoryStatus.ACTIVE);
    }

    @Override
    public boolean exists(UUID recipeCategoryId) {
        return repository.existsByIdAndStatus(recipeCategoryId,RecipeCategoryStatus.ACTIVE);
    }
}
