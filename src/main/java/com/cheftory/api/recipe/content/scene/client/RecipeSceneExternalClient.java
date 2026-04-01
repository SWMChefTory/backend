package com.cheftory.api.recipe.content.scene.client;

import com.cheftory.api.recipe.content.scene.client.dto.ClientRecipeScenesRequest;
import com.cheftory.api.recipe.content.scene.client.dto.ClientRecipeScenesResponse;
import com.cheftory.api.recipe.content.scene.exception.RecipeSceneErrorCode;
import com.cheftory.api.recipe.content.scene.exception.RecipeSceneException;
import com.cheftory.api.recipe.content.step.entity.RecipeStep;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 외부 scene 생성 API 클라이언트 구현체.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RecipeSceneExternalClient implements RecipeSceneClient {
    private final RecipeSceneHttpApi recipeSceneHttpApi;

    @Override
    public ClientRecipeScenesResponse fetch(String fileUri, String mimeType, List<RecipeStep> recipeSteps)
            throws RecipeSceneException {
        ClientRecipeScenesRequest request = ClientRecipeScenesRequest.from(fileUri, mimeType, recipeSteps);
        try {
            return recipeSceneHttpApi.fetch(request);
        } catch (Exception e) {
            log.warn("scene 생성 중 오류 발생", e);
            throw new RecipeSceneException(RecipeSceneErrorCode.RECIPE_SCENE_CREATE_FAIL, e);
        }
    }
}
