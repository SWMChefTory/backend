package com.cheftory.api.recipe.step.helper;

import com.cheftory.api.recipe.step.entity.RecipeStep;
import com.cheftory.api.recipe.step.helper.repository.RecipeStepRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RecipeStepCreator {
    private final RecipeStepRepository recipeStepRepository;

    public List<UUID> createAll(List<RecipeStep> recipeSteps) {
        return recipeStepRepository
                .saveAll(recipeSteps)
                .stream()
                .map(RecipeStep::getId)
                .toList();
    }
}
