package com.cheftory.api.recipe.content.detailMeta.repository;

import com.cheftory.api.recipe.content.detailMeta.entity.RecipeDetailMeta;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * 레시피 상세 메타 정보 JPA Repository
 */
public interface RecipeDetailMetaJpaRepository extends JpaRepository<RecipeDetailMeta, UUID> {
    Optional<RecipeDetailMeta> findByRecipeId(UUID recipeId);

    List<RecipeDetailMeta> findAllByRecipeIdIn(List<UUID> recipeIds);
}
