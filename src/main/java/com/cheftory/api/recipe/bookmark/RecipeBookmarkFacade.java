package com.cheftory.api.recipe.bookmark;

import com.cheftory.api.credit.exception.CreditErrorCode;
import com.cheftory.api.credit.exception.CreditException;
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

    public void createAndCharge(UUID userId, UUID recipeId) {
        RecipeInfo recipeInfo = recipeInfoService.get(recipeId);
        try {
            creditPort.spendRecipeCreate(userId, recipeId, recipeInfo.getCreditCost());
        } catch (CreditException e) {
            if (e.getErrorMessage() == CreditErrorCode.CREDIT_INSUFFICIENT) {
                recipeBookmarkService.delete(userId, recipeId);
                throw new RecipeException(CreditErrorCode.CREDIT_INSUFFICIENT);
            }
            if (e.getErrorMessage() == CreditErrorCode.CREDIT_CONCURRENCY_CONFLICT) {
                recipeBookmarkService.delete(userId, recipeId);
                throw new RecipeException(CreditErrorCode.CREDIT_CONCURRENCY_CONFLICT);
            }
            recipeBookmarkService.delete(userId, recipeId);
            throw e;
        }
    }
}
