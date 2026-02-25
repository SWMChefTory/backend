package com.cheftory.api.recipe.creation.pipeline;

import com.cheftory.api.recipe.content.briefing.RecipeBriefingService;
import com.cheftory.api.recipe.creation.progress.RecipeProgressService;
import com.cheftory.api.recipe.creation.progress.entity.RecipeProgressDetail;
import com.cheftory.api.recipe.creation.progress.entity.RecipeProgressStep;
import com.cheftory.api.recipe.exception.RecipeErrorCode;
import com.cheftory.api.recipe.exception.RecipeException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RecipeCreationBriefingStep implements RecipeCreationPipelineStep {
    private final RecipeBriefingService recipeBriefingService;
    private final RecipeProgressService recipeProgressService;

    /**
     * 레시피 브리핑 생성 단계.
     *
     * <p>브리핑 데이터가 이미 존재하면 생성 호출을 생략하고 해당 jobId 실행의 progress만 성공 처리합니다.</p>
     */
    @Override
    public RecipeCreationExecutionContext run(RecipeCreationExecutionContext context) throws RecipeException {
        if (context.getFileUri() == null || context.getMimeType() == null) {
            throw new RecipeException(RecipeErrorCode.RECIPE_CREATE_FAIL);
        }
        if (recipeBriefingService.exists(context.getRecipeId())) {
            recipeProgressService.success(
                    context.getRecipeId(),
                    RecipeProgressStep.BRIEFING,
                    RecipeProgressDetail.BRIEFING,
                    context.getJobId());
            return context;
        }
        recipeProgressService.start(
                context.getRecipeId(), RecipeProgressStep.BRIEFING, RecipeProgressDetail.BRIEFING, context.getJobId());
        try {
            recipeBriefingService.create(context.getVideoId(), context.getRecipeId());
            recipeProgressService.success(
                    context.getRecipeId(),
                    RecipeProgressStep.BRIEFING,
                    RecipeProgressDetail.BRIEFING,
                    context.getJobId());
            return context;
        } catch (RecipeException ex) {
            recipeProgressService.failed(
                    context.getRecipeId(),
                    RecipeProgressStep.BRIEFING,
                    RecipeProgressDetail.BRIEFING,
                    context.getJobId());
            throw ex;
        }
    }
}
