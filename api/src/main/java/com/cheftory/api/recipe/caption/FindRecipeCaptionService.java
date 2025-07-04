package com.cheftory.api.recipe.caption;

import com.cheftory.api.recipe.caption.dto.CaptionFindResponse;
import com.cheftory.api.recipe.caption.entity.RecipeCaption;
import com.cheftory.api.recipe.caption.repository.RecipeCaptionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class FindRecipeCaptionService {
    private final RecipeCaptionRepository recipeCaptionRepository;
    public CaptionFindResponse findRecipeCaption(UUID recipeInfoId) {
        RecipeCaption recipeCaption = recipeCaptionRepository.findByRecipeInfoId(recipeInfoId);
        return CaptionFindResponse.of(recipeCaption.getSegments());
    }
}
