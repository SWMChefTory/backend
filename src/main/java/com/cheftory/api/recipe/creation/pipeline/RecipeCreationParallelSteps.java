package com.cheftory.api.recipe.creation.pipeline;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

public class RecipeCreationParallelSteps {
    private final Executor executor;
    private final List<RecipeCreationPipelineStep> steps;

    public RecipeCreationParallelSteps(Executor executor, List<RecipeCreationPipelineStep> steps) {
        this.executor = executor;
        this.steps = steps;
    }

    public RecipeCreationExecutionContext run(RecipeCreationExecutionContext context) {
        CompletableFuture<?>[] futures = steps.stream()
                .map(step -> CompletableFuture.runAsync(() -> step.run(context), executor))
                .toArray(CompletableFuture[]::new);

        CompletableFuture.allOf(futures).join();
        return context;
    }
}
