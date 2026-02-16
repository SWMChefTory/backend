package com.cheftory.api.recipe.content.detail.client;

import com.cheftory.api.recipe.content.detail.client.dto.ClientRecipeDetailRequest;
import com.cheftory.api.recipe.content.detail.client.dto.ClientRecipeDetailResponse;
import com.cheftory.api.recipe.exception.RecipeErrorCode;
import com.cheftory.api.recipe.exception.RecipeException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class RecipeDetailClient {
    private final RecipeDetailHttpApi recipeDetailHttpApi;

    public ClientRecipeDetailResponse fetch(String videoId, String fileUri, String mimeType, String originalTitle)
            throws RecipeException {
        try {
            ClientRecipeDetailRequest request =
                    ClientRecipeDetailRequest.from(videoId, fileUri, mimeType, originalTitle);

            return recipeDetailHttpApi.fetch(request);
        } catch (Exception e) {
            log.warn(e.getMessage());
            throw new RecipeException(RecipeErrorCode.RECIPE_CREATE_FAIL, e);
        }
    }
}
