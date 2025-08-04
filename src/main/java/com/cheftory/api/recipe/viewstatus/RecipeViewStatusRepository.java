package com.cheftory.api.recipe.viewstatus;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface RecipeViewStatusRepository extends JpaRepository<RecipeViewStatus, UUID> {

  List<RecipeViewStatus> findByUserId(UUID userId, Sort sort);
  Optional<RecipeViewStatus> findByRecipeIdAndUserId(UUID recipeId, UUID userId);
  List<RecipeViewStatus> findAllByUserIdAndRecipeCategoryId(UUID userId, UUID recipeCategoryId);
  List<RecipeViewStatus> findByRecipeCategoryId(UUID recipeCategoryId);

  @Query("SELECT r.recipeCategoryId as categoryId, COUNT(r) as count " +
      "FROM RecipeViewStatus r " +
      "WHERE r.recipeCategoryId IN :categoryIds " +
      "GROUP BY r.recipeCategoryId")
  List<RecipeViewStatusCountProjection> countByCategoryIds(@Param("categoryIds") List<UUID> categoryIds);

  boolean existsByRecipeIdAndUserId(UUID recipeId, UUID userId);
}
