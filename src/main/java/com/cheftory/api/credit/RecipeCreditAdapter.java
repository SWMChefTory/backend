package com.cheftory.api.credit;

import com.cheftory.api.credit.entity.Credit;
import com.cheftory.api.credit.exception.CreditException;
import com.cheftory.api.recipe.creation.credit.RecipeCreditPort;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * 레시피 생성 관련 크레딧 어댑터.
 * RecipeCreation 모듈의 크레딧 포트를 구현합니다.
 */
@Component
@RequiredArgsConstructor
public class RecipeCreditAdapter implements RecipeCreditPort {
    private final CreditService creditService;

    /**
     * 레시피 생성 시 크레딧을 사용합니다.
     *
     * @param userId 사용자 ID
     * @param recipeId 레시피 ID
     * @param creditCost 소비할 크레딧 양
     * @throws CreditException 크레딧 관련 예외 발생 시
     */
    @Override
    public void spendRecipeCreate(UUID userId, UUID recipeId, long creditCost) throws CreditException {
        creditService.spend(Credit.recipeCreate(userId, recipeId, creditCost));
    }

    /**
     * 레시피 생성 실패 시 크레딧을 환불합니다.
     *
     * @param userId 사용자 ID
     * @param recipeId 레시피 ID
     * @param creditCost 환불할 크레딧 양
     * @throws CreditException 크레딧 관련 예외 발생 시
     */
    @Override
    public void refundRecipeCreate(UUID userId, UUID recipeId, long creditCost) throws CreditException {
        creditService.grant(Credit.recipeCreateRefund(userId, recipeId, creditCost));
    }
}
