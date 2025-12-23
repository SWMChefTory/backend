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
  Page<RecipeInfo> findCuisineRecipes(
      @Param("query") String query,
      @Param("recipeStatus") RecipeStatus recipeStatus,
      Pageable pageable);
}
