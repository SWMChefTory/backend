package com.cheftory.api.recipe.content.scene;

import com.cheftory.api._common.Clock;
import com.cheftory.api.recipe.content.scene.client.RecipeSceneClient;
import com.cheftory.api.recipe.content.scene.client.dto.ClientRecipeScenesResponse;
import com.cheftory.api.recipe.content.scene.entity.RecipeScene;
import com.cheftory.api.recipe.content.scene.exception.RecipeSceneErrorCode;
import com.cheftory.api.recipe.content.scene.exception.RecipeSceneException;
import com.cheftory.api.recipe.content.scene.repository.RecipeSceneRepository;
import com.cheftory.api.recipe.content.step.RecipeStepService;
import com.cheftory.api.recipe.content.step.entity.RecipeStep;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * 레시피 scene 도메인 서비스.
 */
@Service
@RequiredArgsConstructor
public class RecipeSceneService {
    private final RecipeSceneClient recipeSceneClient;
    private final RecipeSceneRepository recipeSceneRepository;
    private final RecipeStepService recipeStepService;
    private final Clock clock;

    public List<UUID> create(UUID recipeId, String fileUri, String mimeType) throws RecipeSceneException {
        List<RecipeStep> recipeSteps = recipeStepService.gets(recipeId);
        if (recipeSteps.isEmpty()) {
            throw new RecipeSceneException(RecipeSceneErrorCode.RECIPE_SCENE_CREATE_FAIL);
        }

        ClientRecipeScenesResponse response = recipeSceneClient.fetch(fileUri, mimeType, recipeSteps);
        List<RecipeScene> recipeScenes = response.toRecipeScenes(recipeId, clock);
        return recipeSceneRepository.create(recipeScenes);
    }

    public boolean exists(UUID recipeId) {
        return recipeSceneRepository.existsByRecipeId(recipeId);
    }
}
