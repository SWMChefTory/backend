package com.cheftory.api.recipeinfo.category;

import com.cheftory.api.recipeinfo.category.entity.RecipeCategory;
import com.cheftory.api.recipeinfo.category.entity.RecipeCategoryStatus;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RecipeCategoryRepository extends JpaRepository<RecipeCategory, UUID> {

  List<RecipeCategory> findAllByUserIdAndStatus(UUID userId, RecipeCategoryStatus status);
}
