package com.cheftory.api.recipe.analysis;

import com.cheftory.api.recipe.caption.entity.RecipeCaption;
import com.cheftory.api.recipe.analysis.client.RecipeAnalysisClient;
import com.cheftory.api.recipe.analysis.client.dto.ClientRecipeAnalysisResponse;
import com.cheftory.api.recipe.analysis.entity.RecipeAnalysis;
import com.cheftory.api.recipe.analysis.exception.RecipeAnalysisErrorCode;
import com.cheftory.api.recipe.analysis.exception.RecipeAnalysisException;
import com.cheftory.api.recipe.analysis.repository.RecipeAnalysisRepository;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RecipeAnalysisService {
    private final RecipeAnalysisClient recipeAnalysisClient;
    private final RecipeAnalysisRepository recipeAnalysisRepository;

    @Transactional
    public UUID create(UUID recipeId, String videoId, RecipeCaption recipeCaption) {
        ClientRecipeAnalysisResponse response = recipeAnalysisClient
                .fetchRecipeIngredients(videoId, recipeCaption);

        RecipeAnalysis recipeAnalysis = RecipeAnalysis.from(response.toIngredients(), recipeId, response.tags(),
            response.servings(), response.cookTime(), response.description());

        recipeAnalysisRepository.save(recipeAnalysis);
    }

    public Optional<RecipeAnalysis> findByRecipeId(UUID recipeId) {
        return recipeAnalysisRepository.findByRecipeId(recipeId);
    }
}
