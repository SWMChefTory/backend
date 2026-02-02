package com.cheftory.api.recipe.creation.progress;

import com.cheftory.api._common.Clock;
import com.cheftory.api.recipe.creation.progress.entity.RecipeProgress;
import com.cheftory.api.recipe.creation.progress.entity.RecipeProgressDetail;
import com.cheftory.api.recipe.creation.progress.entity.RecipeProgressState;
import com.cheftory.api.recipe.creation.progress.entity.RecipeProgressStep;
import com.cheftory.api.recipe.creation.progress.utils.RecipeProgressSort;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class RecipeProgressService {
    private final RecipeProgressRepository recipeProgressRepository;
    private final Clock clock;

    public List<RecipeProgress> gets(UUID recipeId) {
        return recipeProgressRepository.findAllByRecipeId(recipeId, RecipeProgressSort.CREATE_AT_ASC);
    }

    public void start(UUID recipeId, RecipeProgressStep step, RecipeProgressDetail detail) {
        create(recipeId, step, detail, RecipeProgressState.RUNNING);
    }

    public void success(UUID recipeId, RecipeProgressStep step, RecipeProgressDetail detail) {
        create(recipeId, step, detail, RecipeProgressState.SUCCESS);
    }

    public void failed(UUID recipeId, RecipeProgressStep step, RecipeProgressDetail detail) {
        create(recipeId, step, detail, RecipeProgressState.FAILED);
    }

    private void create(
            UUID recipeId, RecipeProgressStep step, RecipeProgressDetail detail, RecipeProgressState state) {
        RecipeProgress recipeProgress = RecipeProgress.create(recipeId, clock, step, detail, state);
        recipeProgressRepository.save(recipeProgress);
    }
}
