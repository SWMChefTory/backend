package com.cheftory.api.recipe.history;

import com.cheftory.api.recipe.history.entity.RecipeHistory;
import com.cheftory.api.recipe.history.entity.RecipeHistoryCategorizedCountProjection;
import com.cheftory.api.recipe.history.entity.RecipeHistoryStatus;
import com.cheftory.api.recipe.history.entity.RecipeHistoryUnCategorizedCountProjection;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface RecipeHistoryRepository extends JpaRepository<RecipeHistory, UUID> {

  @Deprecated(forRemoval = true)
  Page<RecipeHistory> findByUserIdAndStatus(
      UUID userId, RecipeHistoryStatus status, Pageable pageable);

  @Query(
      """
  select h
  from RecipeHistory h
  where h.userId = :userId
    and h.status = :status
  order by h.viewedAt desc, h.id desc
""")
  List<RecipeHistory> findRecentsFirst(UUID userId, RecipeHistoryStatus status, Pageable pageable);

  @Query(
      """
  select h
  from RecipeHistory h
  where h.userId = :userId
    and h.status = :status
    and (
      h.viewedAt < :lastViewedAt
      or (h.viewedAt = :lastViewedAt and h.id < :lastId)
    )
  order by h.viewedAt desc, h.id desc
""")
  List<RecipeHistory> findRecentsKeyset(
      UUID userId,
      RecipeHistoryStatus status,
      LocalDateTime lastViewedAt,
      UUID lastId,
      Pageable pageable);

  Optional<RecipeHistory> findByRecipeIdAndUserIdAndStatus(
      UUID recipeId, UUID userId, RecipeHistoryStatus status);

  @Deprecated(forRemoval = true)
  Page<RecipeHistory> findAllByUserIdAndStatus(
      UUID userId, RecipeHistoryStatus status, Pageable pageable);

  @Deprecated(forRemoval = true)
  Page<RecipeHistory> findAllByUserIdAndRecipeCategoryIdAndStatus(
      UUID userId, UUID recipeCategoryId, RecipeHistoryStatus status, Pageable pageable);

  @Query(
      """
    select h
    from RecipeHistory h
    where h.userId = :userId
      and h.recipeCategoryId = :categoryId
      and h.status = :status
    order by h.viewedAt desc, h.id desc
""")
  List<RecipeHistory> findCategorizedFirst(
      UUID userId, UUID categoryId, RecipeHistoryStatus status, Pageable pageable);

  @Query(
      """
    select h
    from RecipeHistory h
    where h.userId = :userId
      and h.recipeCategoryId = :categoryId
      and h.status = :status
      and (
        h.viewedAt < :lastViewedAt
        or (h.viewedAt = :lastViewedAt and h.id < :lastId)
      )
    order by h.viewedAt desc, h.id desc
""")
  List<RecipeHistory> findCategorizedKeyset(
      UUID userId,
      UUID categoryId,
      RecipeHistoryStatus status,
      LocalDateTime lastViewedAt,
      UUID lastId,
      Pageable pageable);

  @Query(
      """
  select h
  from RecipeHistory h
  where h.userId = :userId
    and h.recipeCategoryId is null
    and h.status = :status
  order by h.viewedAt desc, h.id desc
""")
  List<RecipeHistory> findUncategorizedFirst(
      UUID userId, RecipeHistoryStatus status, Pageable pageable);

  @Query(
      """
  select h
  from RecipeHistory h
  where h.userId = :userId
    and h.recipeCategoryId is null
    and h.status = :status
    and (
      h.viewedAt < :lastViewedAt
      or (h.viewedAt = :lastViewedAt and h.id < :lastId)
    )
  order by h.viewedAt desc, h.id desc
""")
  List<RecipeHistory> findUncategorizedKeyset(
      UUID userId,
      RecipeHistoryStatus status,
      LocalDateTime lastViewedAt,
      UUID lastId,
      Pageable pageable);

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

  boolean existsByUserIdAndRecipeId(UUID userId, UUID recipeId);

  List<RecipeHistory> findByRecipeIdInAndUserIdAndStatus(
      List<UUID> recipeIds, UUID userId, RecipeHistoryStatus status);

  List<RecipeHistory> findAllByRecipeId(UUID recipeId);

  List<RecipeHistory> findAllByRecipeIdAndStatus(UUID recipeId, RecipeHistoryStatus status);

  Optional<RecipeHistory> findByUserIdAndRecipeId(UUID userId, UUID recipeId);
}
