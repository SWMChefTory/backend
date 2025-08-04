package com.cheftory.api.recipe.viewstatus;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ViewStatusRepository extends JpaRepository<RecipeViewStatus, UUID> {

  List<RecipeViewStatus> findByUserId(UUID userId, Sort sort);
  Optional<RecipeViewStatus> findByRecipeIdAndUserId(UUID recipeId, UUID userId);
  List<RecipeViewStatus> findAllByUserIdAndRecipeCategoryId(UUID userId, UUID recipeCategoryId);
  List<RecipeViewStatus> findByRecipeCategoryId(UUID recipeCategoryId);
  @Query("SELECT rv.recipeCategoryId, COUNT(rv) FROM RecipeViewStatus rv WHERE rv.recipeCategoryId IN :categoryIds GROUP BY rv.recipeCategoryId")
  List<RecipeViewStatusCount> countByCategoryIds(@Param("categoryIds") List<UUID> categoryIds);
}
