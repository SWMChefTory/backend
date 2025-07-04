package com.cheftory.api.recipe.service;

import com.cheftory.api.recipe.dto.PreCreationRecipeResponse;
import com.cheftory.api.recipe.dto.RecipeCreateRequest;
import com.cheftory.api.recipe.info.CreateRecipeInfoService;
import com.cheftory.api.recipe.info.FindRecipeInfoService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriComponents;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RecipeService {
    private final CreateRecipeInfoService recipeInfoService;
    private final AsyncRecipeCreationRequester asyncRecipeCreationRequester;
    private final FindRecipeInfoService findRecipeInfoService;
    public PreCreationRecipeResponse CreateRecipe(RecipeCreateRequest recipeCreateRequest) {
        UUID recipeInfoId = recipeInfoService.create(recipeCreateRequest.getVideoUrl());

        asyncRecipeCreationRequester.request(
                recipeInfoId
                ,findRecipeInfoService.getVideoId(recipeInfoId)
        );

        return PreCreationRecipeResponse.of(recipeInfoId);
    }
}
