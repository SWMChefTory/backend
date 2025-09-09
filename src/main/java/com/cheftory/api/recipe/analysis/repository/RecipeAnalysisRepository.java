package com.cheftory.api.recipe.analysis.repository;

import com.cheftory.api.recipe.analysis.entity.RecipeAnalysis;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface RecipeAnalysisRepository extends JpaRepository<RecipeAnalysis, UUID> {
    Optional<RecipeAnalysis> findByRecipeId(UUID recipeId);
}
