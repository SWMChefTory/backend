package com.cheftory.api.recipe.step;

import com.cheftory.api.recipe.step.dto.RecipeStepFindResponse;
import com.cheftory.api.recipe.step.entity.RecipeStep;
import com.cheftory.api.recipe.step.repository.RecipeStepRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@RequiredArgsConstructor
@Service
public class FindRecipeStepService {
    private final RecipeStepRepository repository;

    public List<RecipeStepFindResponse> findAllByRecipeInfoId(UUID recipeInfoId) {
        List<RecipeStep> steps = repository.findByRecipeInfoId(recipeInfoId);

        return steps.stream()
                .map(RecipeStepFindResponse::of)
                .toList();
    }
}
