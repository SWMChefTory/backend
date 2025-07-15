package com.cheftory.api.recipe.helper;

import com.cheftory.api.recipe.entity.Recipe;
import com.cheftory.api.recipe.entity.VideoInfo;
import com.cheftory.api.recipe.helper.repository.RecipeNotFoundException;
import com.cheftory.api.recipe.helper.repository.RecipeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RecipeFinder {
    private final RecipeRepository repository;

    public Recipe findByUri(URI videoUrl) {
        return repository
                .findByVideoUri(videoUrl)
                .orElseThrow(() -> new RecipeNotFoundException("Recipe not found"));
    }


    public Recipe findById(UUID recipeId) {
        return repository
                .findById(recipeId)
                .orElseThrow(() -> new RecipeNotFoundException("Recipe not found"));
    }

    public VideoInfo findVideoInfo(UUID recipeId) {
        return findById(recipeId)
                .getVideoInfo();

    }

    public String findVideoId(UUID recipeId) {
        return findVideoInfo(recipeId)
                .getVideoId();
    }

    public LocalDateTime findCaptionCreatedAt(UUID recipeId) {
        return findById(recipeId)
                .getCaptionCreatedAt();
    }

    public LocalDateTime findIngredientsCreatedAt(UUID recipeId) {
        return findById(recipeId)
                .getIngredientsCreatedAt();
    }

    public LocalDateTime findStepCreatedAt(UUID recipeId) {
        return findById(recipeId)
                .getStepCreatedAt();
    }

    public List<Recipe> findAllRecipes() {
        return repository.findAll();
    }
}
