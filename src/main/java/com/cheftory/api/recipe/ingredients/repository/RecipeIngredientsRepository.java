package com.cheftory.api.recipe.ingredients.repository;

import com.cheftory.api.recipe.entity.Recipe;
import com.cheftory.api.recipe.ingredients.entity.RecipeIngredients;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface RecipeIngredientsRepository extends JpaRepository<RecipeIngredients, UUID> {
    Optional<RecipeIngredients> findByRecipeId(UUID recipeId);
}
