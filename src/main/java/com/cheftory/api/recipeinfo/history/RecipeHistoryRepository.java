package com.cheftory.api.recipeinfo.history;

import com.cheftory.api.recipeinfo.history.entity.RecipeHistory;
import com.cheftory.api.recipeinfo.history.entity.RecipeHistoryCategorizedCountProjection;
import com.cheftory.api.recipeinfo.history.entity.RecipeHistoryStatus;
import com.cheftory.api.recipeinfo.history.entity.RecipeHistoryUnCategorizedCountProjection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface RecipeHistoryRepository extends JpaRepository<RecipeHistory, UUID> {

  Page<RecipeHistory> findByUserIdAndStatus(
      UUID userId, RecipeHistoryStatus status, Pageable pageable);

  Optional<RecipeHistory> findByRecipeIdAndUserIdAndStatus(
      UUID recipeId, UUID userId, RecipeHistoryStatus status);

  Page<RecipeHistory> findAllByUserIdAndStatus(
      UUID userId, RecipeHistoryStatus status, Pageable pageable);

  Page<RecipeHistory> findAllByUserIdAndRecipeCategoryIdAndStatus(
      UUID userId, UUID recipeCategoryId, RecipeHistoryStatus status, Pageable pageable);

  List<RecipeHistory> findByRecipeCategoryIdAndStatus(
      UUID recipeCategoryId, RecipeHistoryStatus status);

  @Query(
      "SELECT r.recipeCategoryId as categoryId, COUNT(r) as count "
          + "FROM RecipeHistory r "
          + "WHERE r.recipeCategoryId IN :categoryIds "
          + "AND r.status = :status "
          + "GROUP BY r.recipeCategoryId")
  List<RecipeHistoryCategorizedCountProjection> countByCategoryIdsAndStatus(
      @Param("categoryIds") List<UUID> categoryIds, @Param("status") RecipeHistoryStatus status);

  @Query(
      "SELECT COUNT(r) as count "
          + "FROM RecipeHistory r "
          + "WHERE r.userId = :userId "
          + "AND r.status = :status")
  RecipeHistoryUnCategorizedCountProjection countByUserIdAndStatus(
      @Param("userId") UUID userId, @Param("status") RecipeHistoryStatus status);

  boolean existsByRecipeIdAndUserIdAndStatus(
      UUID recipeId, UUID userId, RecipeHistoryStatus status);

  List<RecipeHistory> findByRecipeIdInAndUserIdAndStatus(
      List<UUID> recipeIds, UUID userId, RecipeHistoryStatus status);

  List<RecipeHistory> findAllByRecipeId(UUID recipeId);
}
