package com.cheftory.api.recipe.content.step.repository;

import com.cheftory.api.recipe.content.step.entity.RecipeStep;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RecipeStepRepository extends JpaRepository<RecipeStep, UUID> {

  List<RecipeStep> findAllByRecipeId(UUID recipeId, Sort sort);
}
