package com.cheftory.api.recipe.creation.pipeline;

import com.cheftory.api.recipe.content.detail.RecipeDetailService;
import com.cheftory.api.recipe.content.detail.entity.RecipeDetail;
import com.cheftory.api.recipe.content.detailMeta.RecipeDetailMetaService;
import com.cheftory.api.recipe.content.ingredient.RecipeIngredientService;
import com.cheftory.api.recipe.content.tag.RecipeTagService;
import com.cheftory.api.recipe.creation.progress.RecipeProgressService;
import com.cheftory.api.recipe.creation.progress.entity.RecipeProgressDetail;
import com.cheftory.api.recipe.creation.progress.entity.RecipeProgressStep;
import com.cheftory.api.recipe.exception.RecipeErrorCode;
import com.cheftory.api.recipe.exception.RecipeException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RecipeCreationDetailStep implements RecipeCreationPipelineStep {
    private final RecipeDetailService recipeDetailService;
    private final RecipeIngredientService recipeIngredientService;
    private final RecipeTagService recipeTagService;
    private final RecipeDetailMetaService recipeDetailMetaService;
    private final RecipeProgressService recipeProgressService;

    /**
     * 상세 메타(ingredient/tag/detailMeta) 생성 단계.
     *
     * <p>비용 절감을 위해 `recipeId` 기준 결과물이 이미 모두 존재하면 외부 상세 생성 호출을 skip합니다.</p>
     */
    @Override
    public RecipeCreationExecutionContext run(RecipeCreationExecutionContext context) throws RecipeException {
        if (context.getFileUri() == null || context.getMimeType() == null) {
            throw new RecipeException(RecipeErrorCode.RECIPE_CREATE_FAIL);
        }
        boolean hasIngredients = recipeIngredientService.exists(context.getRecipeId());
        boolean hasTags = recipeTagService.exists(context.getRecipeId());
        boolean hasDetailMeta = recipeDetailMetaService.exists(context.getRecipeId());

        if (hasIngredients && hasTags && hasDetailMeta) {
            recipeProgressService.success(
                    context.getRecipeId(),
                    RecipeProgressStep.DETAIL,
                    RecipeProgressDetail.DETAIL_META,
                    context.getJobId());
            return context;
        }

        recipeProgressService.start(
                context.getRecipeId(), RecipeProgressStep.DETAIL, RecipeProgressDetail.INGREDIENT, context.getJobId());
        try {
            RecipeDetail detail = recipeDetailService.getRecipeDetails(
                    context.getVideoId(), context.getFileUri(), context.getMimeType(), context.getTitle());
            recipeIngredientService.create(context.getRecipeId(), detail.ingredients());
            recipeProgressService.success(
                    context.getRecipeId(),
                    RecipeProgressStep.DETAIL,
                    RecipeProgressDetail.INGREDIENT,
                    context.getJobId());
            recipeTagService.create(context.getRecipeId(), detail.tags());
            recipeProgressService.success(
                    context.getRecipeId(), RecipeProgressStep.DETAIL, RecipeProgressDetail.TAG, context.getJobId());
            recipeDetailMetaService.create(
                    context.getRecipeId(), detail.cookTime(), detail.servings(), detail.description(), detail.title());
            recipeProgressService.success(
                    context.getRecipeId(),
                    RecipeProgressStep.DETAIL,
                    RecipeProgressDetail.DETAIL_META,
                    context.getJobId());
            return context;
        } catch (RecipeException ex) {
            recipeProgressService.failed(
                    context.getRecipeId(),
                    RecipeProgressStep.DETAIL,
                    RecipeProgressDetail.DETAIL_META,
                    context.getJobId());
            throw ex;
        }
    }
}
