package com.cheftory.api.recipe.content.scene.repository;

import com.cheftory.api.recipe.content.scene.entity.RecipeScene;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * 레시피 scene JPA 저장소.
 */
public interface RecipeSceneJpaRepository extends JpaRepository<RecipeScene, UUID> {
    boolean existsByRecipeId(UUID recipeId);
}
