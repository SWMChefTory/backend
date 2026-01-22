package com.cheftory.api.recipe.content.detail;

import com.cheftory.api.recipe.content.caption.entity.RecipeCaption;
import com.cheftory.api.recipe.content.detail.client.RecipeDetailClient;
import com.cheftory.api.recipe.content.detail.entity.RecipeDetail;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class RecipeDetailService {

    private final RecipeDetailClient recipeDetailClient;

    public RecipeDetail getRecipeDetails(String videoId, RecipeCaption recipeCaption) {
        return recipeDetailClient.fetchRecipeDetails(videoId, recipeCaption).toRecipeDetail();
    }
}
