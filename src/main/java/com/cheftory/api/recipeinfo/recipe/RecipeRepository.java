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
import org.springframework.data.repository.query.Param;

public interface RecipeRepository extends JpaRepository<Recipe, UUID> {

  @Modifying
  @Transactional
  @Query("update Recipe r set r.viewCount = r.viewCount + 1 where r.id = :id")
  void increaseCount(UUID id);

  List<Recipe> findRecipesByIdInAndRecipeStatusIn(
      List<UUID> recipeIds, List<RecipeStatus> statuses);

  Page<Recipe> findByRecipeStatus(RecipeStatus status, Pageable pageable);

  List<Recipe> findAllByIdIn(List<UUID> ids);

  @Query(
      "SELECT r FROM Recipe r "
          + "WHERE r.recipeStatus = :recipeStatus "
          + "AND EXISTS ("
          + "  SELECT 1 FROM RecipeYoutubeMeta m "
          + "  WHERE m.recipeId = r.id "
          + "  AND CAST(m.type AS string) = :videoType"
          + ")")
  Page<Recipe> findRecipes(
      @Param("recipeStatus") RecipeStatus recipeStatus,
      Pageable pageable,
      @Param("videoType") String videoType);

  @Query(
      "SELECT r FROM Recipe r "
          + "WHERE r.recipeStatus = :recipeStatus "
          + "AND EXISTS ("
          + "  SELECT 1 FROM RecipeTag t "
          + "  WHERE t.recipeId = r.id "
          + "  AND t.tag = :query"
          + ")")
  Page<Recipe> findCuisineRecipes(
      @Param("query") String query,
      @Param("recipeStatus") RecipeStatus recipeStatus,
      Pageable pageable);
}
