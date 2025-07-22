package com.cheftory.api.recipe.model;

import com.cheftory.api.recipe.entity.RecipeStatus;
import com.cheftory.api.recipe.entity.VideoInfo;
import com.cheftory.api.recipe.ingredients.dto.IngredientsInfo;
import com.cheftory.api.recipe.step.dto.RecipeStepInfo;
import com.cheftory.api.recipe.viewstatus.RecipeViewStatusInfo;
import lombok.*;

import java.util.List;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder(access = AccessLevel.PRIVATE)
@Getter
public class FullRecipeInfo {
    private RecipeStatus recipeStatus;
    private VideoInfo videoInfo;
    private IngredientsInfo ingredientsInfo;
    private List<RecipeStepInfo> recipeStepInfos;
    private RecipeViewStatusInfo recipeViewStatusInfo;

    public static FullRecipeInfo of(
            RecipeStatus recipeStatus
            , VideoInfo videoInfo
            , IngredientsInfo ingredientsInfo
            , List<RecipeStepInfo> recipeStepInfos
            , RecipeViewStatusInfo recipeViewStatusInfo
    ) {
        return FullRecipeInfo.builder()
                .recipeStatus(recipeStatus)
                .videoInfo(videoInfo)
                .ingredientsInfo(ingredientsInfo)
                .recipeStepInfos(recipeStepInfos)
                .recipeViewStatusInfo(recipeViewStatusInfo)
                .build();
    }
}
