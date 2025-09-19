package com.cheftory.api.recipeinfo.ingredient;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RecipeIngredientRepository extends JpaRepository<RecipeIngredient, UUID> {
  List<RecipeIngredient> findAllByRecipeId(UUID recipeId);
}
