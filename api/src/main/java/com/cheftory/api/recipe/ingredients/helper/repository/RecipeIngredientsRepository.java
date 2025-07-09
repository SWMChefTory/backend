package com.cheftory.api.recipe.ingredients.helper.repository;

import com.cheftory.api.recipe.ingredients.entity.RecipeIngredients;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface RecipeIngredientsRepository extends JpaRepository<RecipeIngredients, UUID> {
    RecipeIngredients findByRecipeId(UUID recipeId);
    void deleteByRecipeId(UUID recipeId);
}
