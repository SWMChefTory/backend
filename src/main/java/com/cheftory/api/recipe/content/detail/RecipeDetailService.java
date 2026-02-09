package com.cheftory.api.recipe.content.detail;

import com.cheftory.api.recipe.content.detail.client.RecipeDetailClient;
import com.cheftory.api.recipe.content.detail.entity.RecipeDetail;
import com.cheftory.api.recipe.exception.RecipeException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class RecipeDetailService {

    private final RecipeDetailClient client;

    public RecipeDetail getRecipeDetails(String videoId, String fileUri, String mimeType) throws RecipeException {
        return client.fetch(videoId, fileUri, mimeType).toRecipeDetail();
    }
}
