package com.cheftory.api.recipe.creation.pipeline;

import com.cheftory.api.recipe.creation.progress.RecipeProgressService;
import com.cheftory.api.recipe.creation.progress.entity.RecipeProgressDetail;
import com.cheftory.api.recipe.creation.progress.entity.RecipeProgressStep;
import com.cheftory.api.recipe.exception.RecipeException;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.core.task.AsyncTaskExecutor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RecipeCreationPipeline {
    private final RecipeProgressService recipeProgressService;
    private final RecipeCreationLoadYoutubeMetaStep recipeCreationLoadYoutubeMetaStep;
    private final RecipeCreationVerifyStep recipeCreationVerifyStep;
    private final RecipeCreationDetailStep recipeCreationDetailStep;
    private final RecipeCreationInstructionStep recipeCreationInstructionStep;
    private final RecipeCreationBriefingStep recipeCreationBriefingStep;
    private final RecipeCreationFinalizeStep recipeCreationFinalizeStep;
    private final RecipeCreationCleanupStep recipeCreationCleanupStep;
    private final AsyncTaskExecutor recipeCreateExecutor;

    /**
     * 레시피 생성 워크플로우를 실행합니다.
     *
     * <p>`READY` progress 기록 후 `LOAD_YOUTUBE_META -> VERIFY -> (DETAIL/INSTRUCTION/BRIEFING 병렬) -> FINALIZE`
     * 순서로 실행하며, 마지막에는 성공/실패와 무관하게 cleanup step을 호출합니다.</p>
     */
    public void run(RecipeCreationExecutionContext context) throws RecipeException {
        recipeProgressService.start(
                context.getRecipeId(), RecipeProgressStep.READY, RecipeProgressDetail.READY, context.getJobId());

        RecipeCreationExecutionContext updated = recipeCreationLoadYoutubeMetaStep.run(context);
        updated = recipeCreationVerifyStep.run(updated);
        try {
            new RecipeCreationParallelSteps(
                            recipeCreateExecutor,
                            List.of(
                                    recipeCreationDetailStep,
                                    recipeCreationInstructionStep,
                                    recipeCreationBriefingStep))
                    .run(updated);

            recipeCreationFinalizeStep.run(updated);
        } finally {
            recipeCreationCleanupStep.cleanup(updated);
        }
    }
}
