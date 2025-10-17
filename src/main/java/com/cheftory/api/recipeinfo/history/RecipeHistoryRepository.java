package com.cheftory.api.recipeinfo.history;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface RecipeHistoryRepository extends JpaRepository<RecipeHistory, UUID> {

  Page<RecipeHistory> findByUserIdAndStatus(UUID userId, RecipeViewState status, Pageable pageable);

  Optional<RecipeHistory> findByRecipeIdAndUserIdAndStatus(
      UUID recipeId, UUID userId, RecipeViewState status);

  Page<RecipeHistory> findAllByUserIdAndStatus(
      UUID userId, RecipeViewState status, Pageable pageable);

  Page<RecipeHistory> findAllByUserIdAndRecipeCategoryIdAndStatus(
      UUID userId, UUID recipeCategoryId, RecipeViewState status, Pageable pageable);

  List<RecipeHistory> findByRecipeCategoryIdAndStatus(
      UUID recipeCategoryId, RecipeViewState status);

  @Query(
      "SELECT r.recipeCategoryId as categoryId, COUNT(r) as count "
          + "FROM RecipeHistory r "
          + "WHERE r.recipeCategoryId IN :categoryIds "
          + "AND r.status = :status "
          + "GROUP BY r.recipeCategoryId")
  List<RecipeHistoryCountProjection> countByCategoryIdsAndStatus(
      @Param("categoryIds") List<UUID> categoryIds, @Param("status") RecipeViewState status);

  boolean existsByRecipeIdAndUserIdAndStatus(UUID recipeId, UUID userId, RecipeViewState status);

  List<RecipeHistory> findByRecipeIdInAndUserIdAndStatus(
      List<UUID> recipeIds, UUID userId, RecipeViewState status);
}
