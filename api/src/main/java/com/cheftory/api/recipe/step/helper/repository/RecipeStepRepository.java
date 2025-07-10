package com.cheftory.api.recipe.step.helper.repository;

import com.cheftory.api.recipe.step.entity.RecipeStep;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface RecipeStepRepository extends JpaRepository<RecipeStep, UUID> {


    List<RecipeStep> findAllByRecipeId(UUID recipeId);

    void deleteAllByRecipeId(UUID recipeId);
}
