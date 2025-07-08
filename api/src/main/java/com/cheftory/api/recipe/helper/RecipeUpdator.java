package com.cheftory.api.recipe.helper;

import com.cheftory.api.recipe.entity.RecipeStatus;
import com.cheftory.api.recipe.helper.repository.RecipeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class RecipeUpdator {
    private final RecipeRepository recipeRepository;

    public Integer updateState(UUID recipeId, RecipeStatus recipeStatus) {
        return recipeRepository.updateStatus(recipeId, recipeStatus);
    }

    public Integer increseCount(UUID recipeId) {
        return recipeRepository.increaseCount(recipeId);
    }

    public Integer updateCaptionCreatedAt(UUID recipeId,LocalDateTime captionCreatedAt) {
        return recipeRepository
                .updateCaptionCreatedAt(recipeId,captionCreatedAt);
    }

    public Integer updateIngredientsCreatedAt(UUID recipeId,LocalDateTime ingredientsCreatedAt) {
        log.info("updateIngredientsCreatedAt");
        return recipeRepository
                .updateIngredientsCreatedAt(recipeId,ingredientsCreatedAt);
    }

    public Integer updateStepCreatedAt(UUID recipeId,LocalDateTime stepCreatedAt) {
        log.info("updateStepCreatedAt");
        return recipeRepository
                .updateStepCreatedAt(recipeId,stepCreatedAt);
    }
}
