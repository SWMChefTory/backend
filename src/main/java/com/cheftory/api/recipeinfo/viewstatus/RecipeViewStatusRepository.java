package com.cheftory.api.recipeinfo.viewstatus;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface RecipeViewStatusRepository extends JpaRepository<RecipeViewStatus, UUID> {

  Page<RecipeViewStatus> findByUserIdAndStatus(
      UUID userId, RecipeViewState status, Pageable pageable);

  Optional<RecipeViewStatus> findByRecipeIdAndUserIdAndStatus(
      UUID recipeId, UUID userId, RecipeViewState status);

  Page<RecipeViewStatus> findAllByUserIdAndRecipeCategoryIdAndStatus(
      UUID userId, UUID recipeCategoryId, RecipeViewState status, Pageable pageable);

  List<RecipeViewStatus> findByRecipeCategoryIdAndStatus(
      UUID recipeCategoryId, RecipeViewState status);

  @Query(
      "SELECT r.recipeCategoryId as categoryId, COUNT(r) as count "
          + "FROM RecipeViewStatus r "
          + "WHERE r.recipeCategoryId IN :categoryIds "
          + "AND r.status = :status "
          + "GROUP BY r.recipeCategoryId")
  List<RecipeViewStatusCountProjection> countByCategoryIdsAndStatus(
      @Param("categoryIds") List<UUID> categoryIds, @Param("status") RecipeViewState status);

  boolean existsByRecipeIdAndUserIdAndStatus(UUID recipeId, UUID userId, RecipeViewState status);

  List<RecipeViewStatus> findByRecipeIdInAndUserIdAndStatus(
      List<UUID> recipeIds, UUID userId, RecipeViewState status);
}
