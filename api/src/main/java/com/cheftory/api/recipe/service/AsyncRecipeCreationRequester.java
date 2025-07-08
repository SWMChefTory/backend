package com.cheftory.api.temp;

import com.cheftory.api.recipe.caption.RecipeCaptionService;
import com.cheftory.api.recipe.caption.dto.CaptionInfo;
import com.cheftory.api.recipe.entity.RecipeStatus;
import com.cheftory.api.recipe.ingredients.RecipeIngredientsService;
import com.cheftory.api.recipe.ingredients.entity.Ingredient;
import com.cheftory.api.recipe.ingredients.helper.RecipeIngredientsFinder;
import com.cheftory.api.recipe.helper.RecipeUpdator;
import com.cheftory.api.recipe.step.RecipeStepService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@RequiredArgsConstructor
@Service
@Slf4j
public class AsyncRecipeCreationRequester {
    private final RecipeUpdator recipeUpdator;
    private final RecipeCaptionService recipeCaptionService;
    private final RecipeIngredientsService recipeIngredientsService;
    private final RecipeStepService recipeStepService;
    private final RecipeIngredientsFinder recipeIngredientsFinder;

    //어짜피 이거는 RecipeService에서만 쓰고 있는 recipeId만 보내줄건데 recipeId가 있는지 체크할 필요가 있을까?
    //중간에 AI 서버에서 실패했을 때 대응하는 방법이 필요해 보임.
    @Async
    public void request(UUID recipeId){
        UUID captionId = recipeCaptionService.create(recipeId);
        recipeUpdator.updateCaptionCreatedAt(recipeId, LocalDateTime.now());

        CaptionInfo captionInfo = recipeCaptionService.getCaptionInfo(captionId);
        UUID ingredientsId = recipeIngredientsService.create(recipeId,captionInfo);
        recipeUpdator.updateIngredientsCreatedAt(recipeId, LocalDateTime.now());

        List<Ingredient> ingredientsContent = recipeIngredientsFinder
                .findIngredientsContent(ingredientsId);
        recipeStepService.create(recipeId,captionInfo,ingredientsContent);
        recipeUpdator.updateStepCreatedAt(recipeId, LocalDateTime.now());

        recipeUpdator.updateState(recipeId, RecipeStatus.COMPLETED);
    }
}
