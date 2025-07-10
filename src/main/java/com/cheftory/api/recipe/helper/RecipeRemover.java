package com.cheftory.api.recipe.helper;

import com.cheftory.api.recipe.helper.repository.RecipeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RecipeRemover {
    private final RecipeRepository recipeRepository;

    public void removeById(UUID recipeId) {
        recipeRepository.deleteById(recipeId);
    }
}
