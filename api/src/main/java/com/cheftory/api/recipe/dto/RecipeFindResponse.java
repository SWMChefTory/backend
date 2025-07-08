package com.cheftory.api.recipe.dto;
import com.cheftory.api.recipe.entity.RecipeStatus;
import com.cheftory.api.recipe.entity.VideoInfo;
import com.cheftory.api.recipe.ingredients.dto.IngredientsInfo;
import com.cheftory.api.recipe.step.dto.RecipeStepInfo;
import lombok.*;

import java.util.List;

@AllArgsConstructor(access= AccessLevel.PRIVATE)
@Builder(access= AccessLevel.PRIVATE)
@Getter
public class RecipeFindResponse {
    private RecipeStatus recipeStatus;
    private VideoInfo videoInfo;
    private IngredientsInfo ingredientsInfo;
    private List<RecipeStepInfo> recipeStepInfos;

    public static RecipeFindResponse of(RecipeStatus recipeStatus, VideoInfo videoInfo,IngredientsInfo ingredientsInfo, List<RecipeStepInfo> recipeStepInfos) {
        return RecipeFindResponse.builder()
                .recipeStatus(recipeStatus)
                .videoInfo(videoInfo)
                .ingredientsInfo(ingredientsInfo)
                .recipeStepInfos(recipeStepInfos)
                .build();
    }
}
