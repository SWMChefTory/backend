package com.cheftory.api.recipeinfo.progress;

import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RecipeProgressRepository extends JpaRepository<RecipeProgress, UUID> {
  List<RecipeProgress> findAllByRecipeId(UUID recipeId, Sort sort);
}
