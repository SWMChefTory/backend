package com.cheftory.api.recipe.content.scene.client;

import com.cheftory.api.recipe.content.scene.client.dto.ClientRecipeScenesResponse;
import com.cheftory.api.recipe.content.scene.exception.RecipeSceneException;
import com.cheftory.api.recipe.content.step.entity.RecipeStep;
import java.util.List;

/**
 * 외부 scene 생성 API 클라이언트 인터페이스.
 */
public interface RecipeSceneClient {
    ClientRecipeScenesResponse fetch(String fileUri, String mimeType, List<RecipeStep> recipeSteps)
            throws RecipeSceneException;
}
