package com.cheftory.api.recipe.service;

import com.cheftory.api.recipe.caption.FindRecipeCaptionService;
import com.cheftory.api.recipe.caption.dto.CaptionFindResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.UUID;

@RequiredArgsConstructor
@Service
public class AsyncRecipeCreationRequester {
    private final CaptionProcessor captionProcessor;
    private final FindRecipeCaptionService findRecipeCaptionService;
    private final IngredientsProcessor ingredientsProcessor;
    private final StepProcessor stepProcessor;

    //어짜피 이거는 RecipeService에서만 쓰고 있는 recipeId만 보내줄건데 recipeId가 있는지 체크할 필요가 있을까?
    //중간에 ai서버에서 실패했을 때 대응하는 방법이 필요해 보임.
    @Async
    public void request(UUID recipeInfoId,String videoId){
        captionProcessor.process(recipeInfoId,videoId);
        CaptionFindResponse captionFindResponse = findRecipeCaptionService
                .findRecipeCaption(recipeInfoId);
        String segments = captionFindResponse.getSegments();
        ingredientsProcessor.process(recipeInfoId,videoId,segments);
        stepProcessor.process(recipeInfoId,videoId,segments);
    }
}
