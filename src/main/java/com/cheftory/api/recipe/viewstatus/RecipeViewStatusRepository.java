package com.cheftory.api.recipe.viewstatus;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface RecipeViewStatusRepository extends JpaRepository<RecipeViewStatus, UUID> {

  List<RecipeViewStatus> findByUserIdAndStatus(UUID userId, RecipeViewState status, Sort sort);
  Optional<RecipeViewStatus> findByRecipeIdAndUserIdAndStatus(UUID recipeId, UUID userId, RecipeViewState status);
  List<RecipeViewStatus> findAllByUserIdAndRecipeCategoryIdAndStatus(UUID userId, UUID recipeCategoryId, RecipeViewState status);
  List<RecipeViewStatus> findByRecipeCategoryIdAndStatus(UUID recipeCategoryId, RecipeViewState status);

  @Query("SELECT r.recipeCategoryId as categoryId, COUNT(r) as count " +
      "FROM RecipeViewStatus r " +
      "WHERE r.recipeCategoryId IN :categoryIds " +
      "AND r.status = :status " +
      "GROUP BY r.recipeCategoryId")
  List<RecipeViewStatusCountProjection> countByCategoryIdsAndStatus(
      @Param("categoryIds") List<UUID> categoryIds,
      @Param("status") RecipeViewState status
  );
  boolean existsByRecipeIdAndUserIdAndStatus(UUID recipeId, UUID userId, RecipeViewState status);
}
