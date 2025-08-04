package com.cheftory.api.recipe.model;

import com.cheftory.api.recipe.entity.RecipeStatus;
import com.cheftory.api.recipe.entity.VideoInfo;
import com.cheftory.api.recipe.ingredients.entity.RecipeIngredients;
import com.cheftory.api.recipe.step.entity.RecipeStep;
import com.cheftory.api.recipe.viewstatus.RecipeViewStatus;
import jakarta.annotation.Nullable;
import lombok.*;

import java.util.List;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder(access = AccessLevel.PRIVATE)
@Getter
public class FullRecipeInfo {
    private RecipeStatus recipeStatus;
    private VideoInfo videoInfo;
    @Nullable private IngredientsInfo ingredientsInfo;
    private List<RecipeStepInfo> recipeStepInfos;
    private RecipeViewStatusInfo recipeViewStatusInfo;

    public static FullRecipeInfo of(
        RecipeStatus recipeStatus,
        VideoInfo videoInfo,
        @Nullable RecipeIngredients ingredients,
        List<RecipeStep> recipeSteps,
        RecipeViewStatus recipeViewStatus
    ) {
        return FullRecipeInfo.builder()
            .recipeStatus(recipeStatus)
            .videoInfo(videoInfo)
            .ingredientsInfo(ingredients != null ? IngredientsInfo.from(ingredients) : null)
            .recipeStepInfos(recipeSteps.stream()
                .map(RecipeStepInfo::from)
                .toList())
            .recipeViewStatusInfo(RecipeViewStatusInfo.of(recipeViewStatus))
            .build();
    }
}