package com.cheftory.api.recipeinfo.recipe;

import com.cheftory.api.recipeinfo.recipe.entity.Recipe;
import com.cheftory.api.recipeinfo.recipe.entity.RecipeStatus;
import jakarta.transaction.Transactional;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

public interface RecipeRepository extends JpaRepository<Recipe, UUID> {

  @Modifying
  @Transactional
  @Query("update Recipe r set r.viewCount = r.viewCount + 1 where r.id = :id")
  void increaseCount(UUID id);

  List<Recipe> findRecipesByIdInAndRecipeStatusIn(
      List<UUID> recipeIds, List<RecipeStatus> statuses);

  Page<Recipe> findByRecipeStatus(RecipeStatus status, Pageable pageable);

  List<Recipe> findAllByIdIn(List<UUID> ids);
}
