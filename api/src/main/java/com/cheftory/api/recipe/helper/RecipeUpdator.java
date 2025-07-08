package com.cheftory.api.recipe.helper;

import com.cheftory.api.recipe.entity.RecipeStatus;
import com.cheftory.api.recipe.helper.repository.RecipeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RecipeUpdator {
    private final RecipeRepository recipeRepository;

    @Transactional
    public Integer updateState(UUID recipeId, RecipeStatus recipeStatus) {
        return recipeRepository.updateStatus(recipeId, recipeStatus);
    }

    @Transactional
    public Integer increseCount(UUID recipeId) {
        return recipeRepository.increaseCount(recipeId);
    }
}
