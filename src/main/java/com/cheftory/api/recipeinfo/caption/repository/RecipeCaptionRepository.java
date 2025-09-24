package com.cheftory.api.recipeinfo.caption.repository;

import com.cheftory.api.recipeinfo.caption.entity.RecipeCaption;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RecipeCaptionRepository extends JpaRepository<RecipeCaption, UUID> {

  Optional<RecipeCaption> findByRecipeId(UUID recipeId);
}
