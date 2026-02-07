package com.cheftory.api.recipe.bookmark;

import com.cheftory.api.credit.exception.CreditException;
import com.cheftory.api.recipe.content.info.RecipeInfoService;
import com.cheftory.api.recipe.content.info.entity.RecipeInfo;
import com.cheftory.api.recipe.creation.credit.RecipeCreditPort;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RecipeBookmarkFacade {

    private final RecipeBookmarkService recipeBookmarkService;
    private final RecipeInfoService recipeInfoService;
    private final RecipeCreditPort creditPort;

    public void createAndCharge(UUID userId, UUID recipeId) {
        RecipeInfo recipeInfo = recipeInfoService.get(recipeId);
        try {
            boolean created = recipeBookmarkService.create(userId, recipeInfo.getId());
            if (!created) return;
            creditPort.spendRecipeCreate(userId, recipeId, recipeInfo.getCreditCost());
        } catch (CreditException e) {
            recipeBookmarkService.delete(userId, recipeId);
            throw e;
        }
    }
}
