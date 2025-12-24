package com.cheftory.api.recipe.content.caption.repository;

import com.cheftory.api.recipe.content.caption.entity.RecipeCaption;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RecipeCaptionRepository extends JpaRepository<RecipeCaption, UUID> {

  Optional<RecipeCaption> findByRecipeId(UUID recipeId);
}
