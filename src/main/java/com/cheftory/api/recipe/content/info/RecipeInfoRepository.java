package com.cheftory.api.recipe.content.info;

import com.cheftory.api.recipe.content.info.entity.RecipeInfo;
import com.cheftory.api.recipe.content.info.entity.RecipeStatus;
import jakarta.transaction.Transactional;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface RecipeInfoRepository extends JpaRepository<RecipeInfo, UUID> {

  @Modifying
  @Transactional
  @Query("update RecipeInfo r set r.viewCount = r.viewCount + 1 where r.id = :id")
  void increaseCount(UUID id);

  List<RecipeInfo> findRecipesByIdInAndRecipeStatusIn(
      List<UUID> recipeIds, List<RecipeStatus> statuses);

  @Deprecated(forRemoval = true)
  Page<RecipeInfo> findByRecipeStatus(RecipeStatus status, Pageable pageable);

  List<RecipeInfo> findAllByIdIn(List<UUID> ids);

  @Query(
      "SELECT r FROM RecipeInfo r "
          + "WHERE r.recipeStatus = :recipeStatus "
          + "AND EXISTS ("
          + "  SELECT 1 FROM RecipeYoutubeMeta m "
          + "  WHERE m.recipeId = r.id "
          + "  AND CAST(m.type AS string) = :videoType"
          + ")")
  @Deprecated(forRemoval = true)
  Page<RecipeInfo> findRecipes(
      @Param("recipeStatus") RecipeStatus recipeStatus,
      Pageable pageable,
      @Param("videoType") String videoType);

  @Query(
      "SELECT r FROM RecipeInfo r "
          + "WHERE r.recipeStatus = :recipeStatus "
          + "AND EXISTS ("
          + "  SELECT 1 FROM RecipeTag t "
          + "  WHERE t.recipeId = r.id "
          + "  AND t.tag = :query"
          + ")")
  @Deprecated(forRemoval = true)
  Page<RecipeInfo> findCuisineRecipes(
      @Param("query") String query,
      @Param("recipeStatus") RecipeStatus recipeStatus,
      Pageable pageable);

  @Query(
      """
  select r
  from RecipeInfo r
  where r.recipeStatus = :status
    and exists (
      select 1
      from RecipeTag t
      where t.recipeId = r.id
        and t.tag = :tag
    )
  order by r.viewCount desc, r.id desc
""")
  List<RecipeInfo> findCuisineFirst(
      @Param("tag") String tag, @Param("status") RecipeStatus status, Pageable pageable);

  @Query(
      """
  select r
  from RecipeInfo r
  where r.recipeStatus = :status
    and exists (
      select 1
      from RecipeTag t
      where t.recipeId = r.id
        and t.tag = :tag
    )
    and (
      r.viewCount < :lastViewCount
      or (r.viewCount = :lastViewCount and r.id < :lastId)
    )
  order by r.viewCount desc, r.id desc
""")
  List<RecipeInfo> findCuisineKeyset(
      @Param("tag") String tag,
      @Param("status") RecipeStatus status,
      @Param("lastViewCount") long lastViewCount,
      @Param("lastId") UUID lastId,
      Pageable pageable);

  @Query(
      """
  select r
  from RecipeInfo r
  where r.recipeStatus = :status
  order by r.viewCount desc, r.id desc
""")
  List<RecipeInfo> findPopularFirst(@Param("status") RecipeStatus status, Pageable pageable);

  @Query(
      """
  select r
  from RecipeInfo r
  where r.recipeStatus = :status
    and (
      r.viewCount < :lastViewCount
      or (r.viewCount = :lastViewCount and r.id < :lastId)
    )
  order by r.viewCount desc, r.id desc
""")
  List<RecipeInfo> findPopularKeyset(
      @Param("status") RecipeStatus status,
      @Param("lastViewCount") long lastViewCount,
      @Param("lastId") UUID lastId,
      Pageable pageable);

  @Query(
      """
  select r
  from RecipeInfo r
  where r.recipeStatus = :status
    and exists (
      select 1
      from RecipeYoutubeMeta m
      where m.recipeId = r.id
        and cast(m.type as string) = :videoType
    )
  order by r.viewCount desc, r.id desc
""")
  List<RecipeInfo> findPopularByVideoTypeFirst(
      @Param("status") RecipeStatus status,
      @Param("videoType") String videoType,
      Pageable pageable);

  @Query(
      """
  select r
  from RecipeInfo r
  where r.recipeStatus = :status
    and exists (
      select 1
      from RecipeYoutubeMeta m
      where m.recipeId = r.id
        and cast(m.type as string) = :videoType
    )
    and (
      r.viewCount < :lastViewCount
      or (r.viewCount = :lastViewCount and r.id < :lastId)
    )
  order by r.viewCount desc, r.id desc
""")
  List<RecipeInfo> findPopularByVideoTypeKeyset(
      @Param("status") RecipeStatus status,
      @Param("videoType") String videoType,
      @Param("lastViewCount") long lastViewCount,
      @Param("lastId") UUID lastId,
      Pageable pageable);
}
