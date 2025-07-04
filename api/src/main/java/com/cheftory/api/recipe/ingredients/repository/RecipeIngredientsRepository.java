package com.cheftory.api.recipe.ingredients.repository;

import com.cheftory.api.recipe.ingredients.entity.Ingredients;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface RecipeIngredientsRepository extends JpaRepository<Ingredients,UUID> {
    Ingredients findByRecipeInfoId(UUID recipeInfoId);
}
