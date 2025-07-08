package com.cheftory.api.recipe.caption.helper;

import com.cheftory.api.recipe.caption.entity.RecipeCaption;
import com.cheftory.api.recipe.caption.helper.repository.RecipeCaptionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RecipeCaptionCreator {
    private final RecipeCaptionRepository recipeCaptionRepository;

    public UUID create(RecipeCaption recipeCaption) {
        return recipeCaptionRepository
                .save(recipeCaption)
                .getId();
    }
}
