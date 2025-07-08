package com.cheftory.api.recipe;

import com.cheftory.api.recipe.dto.*;
import com.cheftory.api.recipe.entity.Recipe;
import com.cheftory.api.recipe.entity.RecipeStatus;
import com.cheftory.api.recipe.entity.VideoInfo;
import com.cheftory.api.recipe.helper.RecipeCreator;
import com.cheftory.api.recipe.helper.RecipeFinder;
import com.cheftory.api.recipe.helper.RecipeUpdator;
import com.cheftory.api.recipe.helper.RecipeChecker;
import com.cheftory.api.recipe.ingredients.RecipeIngredientsService;
import com.cheftory.api.recipe.ingredients.dto.IngredientsInfo;
import com.cheftory.api.recipe.client.VideoInfoClient;
import com.cheftory.api.recipe.service.AsyncRecipeCreationService;
import com.cheftory.api.recipe.step.RecipeStepService;
import com.cheftory.api.recipe.step.dto.RecipeStepInfo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriComponents;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class RecipeService {
    private final VideoInfoClient videoInfoClient;
    private final RecipeFinder recipeFinder;
    private final RecipeCreator recipeCreator;
    private final AsyncRecipeCreationService asyncRecipeCreationService;
    private final RecipeStepService recipeStepService;
    private final RecipeIngredientsService recipeIngredientsService;
    private final RecipeUpdator recipeUpdator;
    private final RecipeChecker recipeChecker;


    public UUID create(UriComponents uri) {
        if(recipeChecker.checkAlreadyCreated(uri.toUri())){
            Recipe recipe = recipeFinder.findByUri(uri.toUri());
            recipe.isBanned();
            return recipe.getId();
        }

        VideoInfo videoInfo = videoInfoClient.fetchVideoInfo(uri);
        UUID recipeId = recipeCreator.create(videoInfo);
        asyncRecipeCreationService.create(recipeId);
        return recipeId;
    }

    public RecipeFindResponse findTotalRecipeInfo(UUID recipeId) {
        VideoInfo videoInfo = recipeFinder.findVideoInfo(recipeId);

        ensureRecipeFullyCreated(recipeId);
        
        List<RecipeStepInfo> recipeInfos = recipeStepService
                .getRecipeStepInfos(recipeId);
        IngredientsInfo ingredientsInfo = recipeIngredientsService
                .getIngredientsInfoOfRecipe(recipeId);

        recipeUpdator.increseCount(recipeId);
        return RecipeFindResponse.of(RecipeStatus.COMPLETED,videoInfo,ingredientsInfo,recipeInfos);
    }


    private void ensureRecipeFullyCreated(UUID recipeId) {
        LocalDateTime captionCreatedAt = recipeFinder.findCaptionCreatedAt(recipeId);
        LocalDateTime ingredientsCreatedAt = recipeFinder.findIngredientsCreatedAt(recipeId);
        LocalDateTime stepCreatedAt = recipeFinder.findStepCreatedAt(recipeId);

        if(Objects.isNull(captionCreatedAt)){
            throw new RecipeCreationPendingException(RecipeCreationState.EXTRACTING_CAPTION);
        }
        
        if(Objects.isNull(ingredientsCreatedAt)){
            throw new RecipeCreationPendingException(RecipeCreationState.CREATING_INGREDIENTS);
        }
        
        if(Objects.isNull(stepCreatedAt)){
            throw new RecipeCreationPendingException(RecipeCreationState.CREATING_STEPS);
        }
    }


    public List<RecipeOverview> findAllOverviewRecipes() {
        List<Recipe> recipes = recipeFinder.findAllRecipes();
        return recipes.stream()
                .map(RecipeOverview::of)
                .toList();
    }

    public RecipeOverviewsResponse findRecipeOverviewsResponse() {
        return RecipeOverviewsResponse
                .of(findAllOverviewRecipes());
    }
}
