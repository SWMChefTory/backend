package com.cheftory.api.recipe.dto;

import com.cheftory.api.recipe.content.info.entity.RecipeInfo;
import com.cheftory.api.recipe.creation.progress.entity.RecipeProgress;
import java.util.List;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
public class RecipeProgressStatus {

    private List<RecipeProgress> progresses;
    private RecipeInfo recipe;

    public static RecipeProgressStatus of(RecipeInfo recipe, List<RecipeProgress> progresses) {
        return new RecipeProgressStatus(progresses, recipe);
    }
}
