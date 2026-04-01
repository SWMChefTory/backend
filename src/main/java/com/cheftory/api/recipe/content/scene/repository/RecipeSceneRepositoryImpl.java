package com.cheftory.api.recipe.content.scene.repository;

import com.cheftory.api._common.aspect.DbThrottled;
import com.cheftory.api.recipe.content.scene.entity.RecipeScene;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

/**
 * 레시피 scene 저장소 구현체.
 */
@Repository
@RequiredArgsConstructor
public class RecipeSceneRepositoryImpl implements RecipeSceneRepository {

    private final RecipeSceneJpaRepository repository;

    @Override
    public boolean existsByRecipeId(UUID recipeId) {
        return repository.existsByRecipeId(recipeId);
    }

    @DbThrottled
    @Override
    public List<UUID> create(List<RecipeScene> recipeScenes) {
        return repository.saveAll(recipeScenes).stream().map(RecipeScene::getId).toList();
    }
}
