package com.cheftory.api.recipe.caption.helper;

import com.cheftory.api.recipe.caption.helper.repository.RecipeCaptionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RecipeCaptionRemover {
    private final RecipeCaptionRepository recipeCaptionRepository;
    public void removeByRecipeId(UUID recipeId) {
        recipeCaptionRepository.deleteById(recipeId);
    }
}
