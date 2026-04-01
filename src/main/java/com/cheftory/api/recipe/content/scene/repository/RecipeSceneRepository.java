package com.cheftory.api.recipe.content.scene.repository;

import com.cheftory.api.recipe.content.scene.entity.RecipeScene;
import java.util.List;
import java.util.UUID;

/**
 * 레시피 scene 저장소 인터페이스.
 */
public interface RecipeSceneRepository {
    boolean existsByRecipeId(UUID recipeId);

    List<UUID> create(List<RecipeScene> recipeScenes);
}
