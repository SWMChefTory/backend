package com.cheftory.api.recipe.bookmark;

import com.cheftory.api.credit.exception.CreditException;
import com.cheftory.api.recipe.bookmark.exception.RecipeBookmarkException;
import com.cheftory.api.recipe.content.info.RecipeInfoService;
import com.cheftory.api.recipe.content.info.entity.RecipeInfo;
import com.cheftory.api.recipe.content.info.exception.RecipeInfoException;
import com.cheftory.api.recipe.creation.credit.RecipeCreditPort;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * 레시피 북마크 생성과 크레딧 차감을 트랜잭션으로 처리하는 퍼사드
 */
@Service
@RequiredArgsConstructor
public class RecipeBookmarkFacade {

    private final RecipeBookmarkService recipeBookmarkService;
    private final RecipeInfoService recipeInfoService;
    private final RecipeCreditPort creditPort;

    /**
     * 레시피 북마크 생성 및 크레딧 차감
     * 크레딧 차감 실패 시 북마크를 롤백함
     *
     * @param userId 사용자 ID
     * @param recipeId 레시피 ID
     * @throws CreditException 크레딧 차감 실패 시
     */
    public void create(UUID userId, UUID recipeId)
            throws CreditException, RecipeBookmarkException, RecipeInfoException {
        RecipeInfo recipeInfo = recipeInfoService.get(recipeId);
        boolean created = recipeBookmarkService.create(userId, recipeInfo.getId());
        if (!created) return;
        creditPort.spendRecipeCreate(userId, recipeId, recipeInfo.getCreditCost());
    }
}
