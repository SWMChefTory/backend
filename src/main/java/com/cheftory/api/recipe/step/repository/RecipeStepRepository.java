package com.cheftory.api.recipe.step.repository;

import com.cheftory.api.recipe.step.entity.RecipeStep;
import com.cheftory.api.recipe.step.entity.RecipeStepSort;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface RecipeStepRepository extends JpaRepository<RecipeStep, UUID> {


    List<RecipeStep> findAllByRecipeId(UUID recipeId, Sort sort);
}
