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
 * 레시피 북마크 생성과 크레딧 차감을 처리하는 퍼사드
 */
@Service
@RequiredArgsConstructor
public class RecipeBookmarkFacade {

    private final RecipeBookmarkService recipeBookmarkService;
    private final RecipeInfoService recipeInfoService;
    private final RecipeCreditPort creditPort;

    /**
     * 레시피 북마크 생성 및 크레딧 차감
     *
     * <p>레시피 정보를 조회한 후 북마크를 생성하고, 해당 레시피의 설정된 비용만큼 유저의 크레딧을 차감합니다.
     * 크레딧 차감 실패 시 생성된 북마크를 수동으로 삭제하여 롤백 처리합니다.</p>
     *
     * @param userId 사용자 ID
     * @param recipeId 레시피 ID
     * @throws CreditException 크레딧이 부족하거나 차감에 실패했을 때
     * @throws RecipeBookmarkException 북마크 생성 중 예외 발생 시
     * @throws RecipeInfoException 레시피 정보를 찾을 수 없을 때
     */
    public void create(UUID userId, UUID recipeId)
            throws CreditException, RecipeBookmarkException, RecipeInfoException {
        RecipeInfo recipeInfo = recipeInfoService.get(recipeId);
        boolean created = recipeBookmarkService.create(userId, recipeInfo.getId());
        if (!created) return;
        try {
            creditPort.spendRecipeCreate(userId, recipeId, recipeInfo.getCreditCost());
        } catch (CreditException e) {
            recipeBookmarkService.delete(userId, recipeId);
            throw e;
        }
    }
}
