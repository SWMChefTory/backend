package com.cheftory.api.recipe.watched.category;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RecipeCategoryRepository extends JpaRepository<RecipeCategory, UUID> {

  List<RecipeCategory> findAllByUserIdAndStatus(UUID userId, RecipeCategoryStatus status);

  List<RecipeCategory> findAllByIdInAndStatus(List<UUID> ids, RecipeCategoryStatus status);
}
