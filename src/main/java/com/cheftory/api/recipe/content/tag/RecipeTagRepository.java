package com.cheftory.api.recipe.content.tag;

import com.cheftory.api.recipe.content.tag.entity.RecipeTag;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RecipeTagRepository extends JpaRepository<RecipeTag, UUID> {
  List<RecipeTag> findAllByRecipeId(UUID recipeId);

  List<RecipeTag> findAllByRecipeIdIn(List<UUID> recipeIds);
}
