package com.cheftory.api.recipe.creation.progress;

import com.cheftory.api.recipe.creation.progress.entity.RecipeProgress;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RecipeProgressRepository extends JpaRepository<RecipeProgress, UUID> {
  List<RecipeProgress> findAllByRecipeId(UUID recipeId, Sort sort);
}
