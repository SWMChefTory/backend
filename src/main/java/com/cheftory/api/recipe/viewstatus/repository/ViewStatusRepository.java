package com.cheftory.api.recipe.viewstatus.repository;

import com.cheftory.api.recipe.viewstatus.RecipeViewStatus;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ViewStatusRepository extends JpaRepository<RecipeViewStatus, UUID> {

  List<RecipeViewStatus> findByUserId(UUID userId, Sort sort);
  Optional<RecipeViewStatus> findByRecipeIdAndUserId(UUID recipeId, UUID userId);
}
