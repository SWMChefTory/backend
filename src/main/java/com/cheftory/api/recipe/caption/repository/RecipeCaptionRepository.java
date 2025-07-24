package com.cheftory.api.recipe.caption.repository;

import com.cheftory.api.recipe.caption.entity.RecipeCaption;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface RecipeCaptionRepository extends JpaRepository<RecipeCaption, UUID> {

  Optional<RecipeCaption> findByRecipeId(UUID recipeId);
}
