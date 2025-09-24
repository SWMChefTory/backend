package com.cheftory.api.recipeinfo.detailMeta;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RecipeDetailMetaRepository extends JpaRepository<RecipeDetailMeta, UUID> {
  Optional<RecipeDetailMeta> findByRecipeId(UUID recipeId);

  List<RecipeDetailMeta> findAllByRecipeIdIn(List<UUID> recipeIds);
}
