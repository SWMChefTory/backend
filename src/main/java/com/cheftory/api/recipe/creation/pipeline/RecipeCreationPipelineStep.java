package com.cheftory.api.recipe.creation.pipeline;

public interface RecipeCreationPipelineStep {
    RecipeCreationExecutionContext run(RecipeCreationExecutionContext context);
}
