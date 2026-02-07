package com.cheftory.api.recipe.creation.pipeline;

import com.cheftory.api.recipe.creation.progress.RecipeProgressService;
import com.cheftory.api.recipe.creation.progress.entity.RecipeProgressDetail;
import com.cheftory.api.recipe.creation.progress.entity.RecipeProgressStep;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.core.task.AsyncTaskExecutor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RecipeCreationPipeline {
    private final RecipeProgressService recipeProgressService;
    private final RecipeCreationVerifyStep recipeCreationVerifyStep;
    private final RecipeCreationDetailStep recipeCreationDetailStep;
    private final RecipeCreationInstructionStep recipeCreationInstructionStep;
    private final RecipeCreationBriefingStep recipeCreationBriefingStep;
    private final RecipeCreationFinalizeStep recipeCreationFinalizeStep;
    private final RecipeCreationCleanupStep recipeCreationCleanupStep;
    private final AsyncTaskExecutor recipeCreateExecutor;

    public void run(RecipeCreationExecutionContext context) {
        recipeProgressService.start(context.getRecipeId(), RecipeProgressStep.READY, RecipeProgressDetail.READY);

        RecipeCreationExecutionContext updated = recipeCreationVerifyStep.run(context);
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
