package com.cheftory.api.recipe.creation.pipeline;

import com.cheftory.api.recipe.exception.RecipeException;

public interface RecipeCreationPipelineStep {
    RecipeCreationExecutionContext run(RecipeCreationExecutionContext context) throws RecipeException;
}
