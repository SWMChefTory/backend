package com.cheftory.api.recipe.category.repository;

import com.cheftory.api.recipe.category.entity.RecipeCategory;
import com.cheftory.api.recipe.category.exception.RecipeCategoryException;
import java.util.List;
import java.util.UUID;

public interface RecipeCategoryRepository {
    UUID create(RecipeCategory recipeCategory);

    void delete(UUID userId, UUID recipeCategoryId) throws RecipeCategoryException;

    List<RecipeCategory> gets(UUID userId);

    boolean exists(UUID recipeCategoryId);
}
