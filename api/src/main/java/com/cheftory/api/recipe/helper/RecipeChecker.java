package com.cheftory.api.recipe.helper;

import com.cheftory.api.recipe.helper.repository.RecipeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.net.URI;

@Service
@RequiredArgsConstructor
public class RecipeChecker {
    private final RecipeRepository recipeRepository;
    public boolean checkAlreadyCreated(URI videoUrl) {
        return recipeRepository.existsByVideoInfo_VideoUri(videoUrl);
    }
}
