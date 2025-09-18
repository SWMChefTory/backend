package com.cheftory.api.recipe.model;

import com.cheftory.api.recipe.entity.RecipeStatus;
import com.cheftory.api.recipe.entity.VideoInfo;
import com.cheftory.api.recipe.analysis.entity.RecipeAnalysis;
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
    @Nullable
    private RecipeAnalysis recipeAnalysis;
    private List<RecipeStep> recipeStepInfos;
    private RecipeViewStatus recipeViewStatus;

    public static FullRecipeInfo of(
        RecipeStatus recipeStatus,
        VideoInfo videoInfo,
        @Nullable RecipeAnalysis ingredients,
        List<RecipeStep> recipeSteps,
        RecipeViewStatus recipeViewStatus
    ) {
        return FullRecipeInfo.builder()
            .recipeStatus(recipeStatus)
            .videoInfo(videoInfo)
            .recipeAnalysis(ingredients)
            .recipeStepInfos(recipeSteps)
            .recipeViewStatus(recipeViewStatus)
            .build();
    }
}