package com.cheftory.api.recipe.bookmark.repository;

import com.cheftory.api.recipe.bookmark.entity.RecipeBookmark;
import com.cheftory.api.recipe.bookmark.entity.RecipeBookmarkCategorizedCountProjection;
import com.cheftory.api.recipe.bookmark.entity.RecipeBookmarkStatus;
import com.cheftory.api.recipe.bookmark.entity.RecipeBookmarkUnCategorizedCountProjection;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface RecipeBookmarkJpaRepository extends JpaRepository<RecipeBookmark, UUID> {

    @Query(
            """
  select h
  from RecipeBookmark as h
  where h.userId = :userId
    and h.status = :status
  order by h.viewedAt desc, h.id desc
""")
    List<RecipeBookmark> findRecentsFirst(UUID userId, RecipeBookmarkStatus status, Pageable pageable);

    @Query(
            """
  select h
  from RecipeBookmark as h
  where h.userId = :userId
    and h.status = :status
    and (
      h.viewedAt < :lastViewedAt
      or (h.viewedAt = :lastViewedAt and h.id < :lastId)
    )
  order by h.viewedAt desc, h.id desc
""")
    List<RecipeBookmark> findRecentsKeyset(
            UUID userId, RecipeBookmarkStatus status, LocalDateTime lastViewedAt, UUID lastId, Pageable pageable);

    Optional<RecipeBookmark> findByRecipeIdAndUserIdAndStatus(UUID recipeId, UUID userId, RecipeBookmarkStatus status);

    @Query(
            """
    select h
    from RecipeBookmark as h
    where h.userId = :userId
      and h.recipeCategoryId = :categoryId
      and h.status = :status
    order by h.viewedAt desc, h.id desc
""")
    List<RecipeBookmark> findCategorizedFirst(
            UUID userId, UUID categoryId, RecipeBookmarkStatus status, Pageable pageable);

    @Query(
            """
    select h
    from RecipeBookmark as h
    where h.userId = :userId
      and h.recipeCategoryId = :categoryId
      and h.status = :status
      and (
        h.viewedAt < :lastViewedAt
        or (h.viewedAt = :lastViewedAt and h.id < :lastId)
      )
    order by h.viewedAt desc, h.id desc
""")
    List<RecipeBookmark> findCategorizedKeyset(
            UUID userId,
            UUID categoryId,
            RecipeBookmarkStatus status,
            LocalDateTime lastViewedAt,
            UUID lastId,
            Pageable pageable);

    List<RecipeBookmark> findByRecipeCategoryIdAndStatus(UUID recipeCategoryId, RecipeBookmarkStatus status);

    @Query("SELECT r.recipeCategoryId as categoryId, COUNT(r) as count "
            + "FROM RecipeBookmark as r "
            + "WHERE r.recipeCategoryId IN :categoryIds "
            + "AND r.status = :status "
            + "GROUP BY r.recipeCategoryId")
    List<RecipeBookmarkCategorizedCountProjection> countByCategoryIdsAndStatus(
            @Param("categoryIds") List<UUID> categoryIds, @Param("status") RecipeBookmarkStatus status);

    @Query(
            """
    SELECT COUNT(r) as count
      FROM RecipeBookmark r
     WHERE r.userId = :userId
       AND r.status = :status
       AND r.recipeCategoryId IS NULL
""")
    RecipeBookmarkUnCategorizedCountProjection countUncategorizedByUserIdAndStatus(
            @Param("userId") UUID userId, @Param("status") RecipeBookmarkStatus status);

    boolean existsByRecipeIdAndUserIdAndStatus(UUID recipeId, UUID userId, RecipeBookmarkStatus status);

    boolean existsByUserIdAndRecipeId(UUID userId, UUID recipeId);

    List<RecipeBookmark> findByRecipeIdInAndUserIdAndStatus(
            List<UUID> recipeIds, UUID userId, RecipeBookmarkStatus status);

    List<RecipeBookmark> findAllByRecipeId(UUID recipeId);

    List<RecipeBookmark> findAllByRecipeIdAndStatus(UUID recipeId, RecipeBookmarkStatus status);

    List<RecipeBookmark> findAllByIdIn(List<UUID> recipeBookmarkIds);

    Optional<RecipeBookmark> findByUserIdAndRecipeId(UUID userId, UUID recipeId);
}
