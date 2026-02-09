package com.cheftory.api.recipe.content.verify;

import com.cheftory.api.recipe.content.verify.client.RecipeVerifyClient;
import com.cheftory.api.recipe.content.verify.dto.RecipeVerifyClientResponse;
import com.cheftory.api.recipe.content.verify.exception.RecipeVerifyException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RecipeVerifyService {

    private final RecipeVerifyClient recipeVerifyClient;

    public RecipeVerifyClientResponse verify(String videoId) throws RecipeVerifyException {
        return recipeVerifyClient.verifyVideo(videoId);
    }

    public void cleanup(String fileUri) {
        recipeVerifyClient.cleanupVideo(fileUri);
    }
}
