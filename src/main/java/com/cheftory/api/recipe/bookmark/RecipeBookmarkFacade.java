package com.cheftory.api.recipe.bookmark;

import com.cheftory.api.recipe.content.info.RecipeInfoService;
import com.cheftory.api.recipe.content.info.entity.RecipeInfo;
import com.cheftory.api.recipe.content.info.exception.RecipeInfoErrorCode;
import com.cheftory.api.recipe.creation.credit.RecipeCreditPort;
import com.cheftory.api.recipe.exception.RecipeErrorCode;
import com.cheftory.api.recipe.exception.RecipeException;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RecipeBookmarkFacade {

    private final RecipeBookmarkService recipeBookmarkService;
    private final RecipeInfoService recipeInfoService;
    private final RecipeCreditPort creditPort;

    public boolean createAndCharge(UUID userId, UUID recipeId) {
        try {
            RecipeInfo recipeInfo = recipeInfoService.get(recipeId);
            boolean created = recipeBookmarkService.create(userId, recipeId);
            if (created) {
                creditPort.spendRecipeCreate(userId, recipeId, recipeInfo.getCreditCost());
            }
            return created;
        } catch (RecipeException e) {
            if (e.getErrorMessage() == RecipeInfoErrorCode.RECIPE_INFO_NOT_FOUND) {
                throw new RecipeException(RecipeErrorCode.RECIPE_NOT_FOUND);
            }
            throw e;
        }
    }
}
