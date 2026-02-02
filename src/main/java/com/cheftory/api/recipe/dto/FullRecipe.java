package com.cheftory.api.recipe.dto;

import com.cheftory.api.recipe.bookmark.entity.RecipeBookmark;
import com.cheftory.api.recipe.content.briefing.entity.RecipeBriefing;
import com.cheftory.api.recipe.content.detailMeta.entity.RecipeDetailMeta;
import com.cheftory.api.recipe.content.info.entity.RecipeInfo;
import com.cheftory.api.recipe.content.ingredient.entity.RecipeIngredient;
import com.cheftory.api.recipe.content.step.entity.RecipeStep;
import com.cheftory.api.recipe.content.tag.entity.RecipeTag;
import com.cheftory.api.recipe.content.youtubemeta.entity.RecipeYoutubeMeta;
import com.cheftory.api.recipe.creation.progress.entity.RecipeProgress;
import jakarta.annotation.Nullable;
import java.util.List;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
public class FullRecipe {
    private List<RecipeIngredient> recipeIngredients;
    private List<RecipeTag> recipeTags;

    @Nullable
    private RecipeDetailMeta recipeDetailMeta;

    private List<RecipeStep> recipeSteps;
    private List<RecipeProgress> recipeProgresses;

    @Nullable
    private RecipeBookmark recipeBookmark;

    private RecipeYoutubeMeta recipeYoutubeMeta;
    private RecipeInfo recipe;
    private List<RecipeBriefing> recipeBriefings;

    public static FullRecipe of(
            List<RecipeStep> recipeSteps,
            List<RecipeIngredient> recipeIngredients,
            @Nullable RecipeDetailMeta recipeDetailMeta,
            List<RecipeProgress> recipeProgresses,
            List<RecipeTag> recipeTags,
            RecipeYoutubeMeta recipeYoutubeMeta,
            @Nullable RecipeBookmark recipeBookmark,
            RecipeInfo recipe,
            List<RecipeBriefing> recipeBriefings) {

        return new FullRecipe(
                recipeIngredients,
                recipeTags,
                recipeDetailMeta,
                recipeSteps,
                recipeProgresses,
                recipeBookmark,
                recipeYoutubeMeta,
                recipe,
                recipeBriefings);
    }
}
