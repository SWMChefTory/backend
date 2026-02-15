package com.cheftory.api.recipe.creation.pipeline;

import com.cheftory.api.recipe.exception.RecipeErrorCode;
import com.cheftory.api.recipe.exception.RecipeException;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.Executor;

public class RecipeCreationParallelSteps {
    private final Executor executor;
    private final List<RecipeCreationPipelineStep> steps;

    public RecipeCreationParallelSteps(Executor executor, List<RecipeCreationPipelineStep> steps) {
        this.executor = executor;
        this.steps = steps;
    }

    public RecipeCreationExecutionContext run(RecipeCreationExecutionContext context) throws RecipeException {
        CompletableFuture<?>[] futures = steps.stream()
                .map(step -> CompletableFuture.runAsync(
                        () -> {
                            try {
                                step.run(context);
                            } catch (RecipeException e) {
                                throw new CompletionException(e);
                            } catch (Exception e) {
                                throw new CompletionException(
                                        new RecipeException(RecipeErrorCode.RECIPE_CREATE_FAIL, e));
                            }
                        },
                        executor))
                .toArray(CompletableFuture[]::new);

        try {
            CompletableFuture.allOf(futures).join();
        } catch (CompletionException e) {
            Throwable cause = e.getCause();
            if (cause instanceof RecipeException re) {
                throw re;
            }
            throw new RecipeException(RecipeErrorCode.RECIPE_CREATE_FAIL, cause);
        }

        return context;
    }
}
