package com.cheftory.api.recipe.service;

import com.cheftory.api.recipe.caption.CreateRecipeCaptionService;
import com.cheftory.api.recipe.info.UpdateRecipeInfoService;
import com.cheftory.api.recipe.info.entity.RecipeStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;


@Service
@RequiredArgsConstructor
public class CaptionProcessor {
    private final UpdateRecipeInfoService updateRecipeInfoService;
    private final CreateRecipeCaptionService createRecipeCaptionService;

    @Transactional
    public UUID process(UUID recipeId,String videoId){
        updateRecipeInfoService.updateState(recipeId, RecipeStatus.CREATING_CAPTION);
        return createRecipeCaptionService
                .create(videoId,recipeId);
    }
}
