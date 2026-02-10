package com.cheftory.api.recipe.creation.credit;

import com.cheftory.api.credit.exception.CreditException;
import java.util.UUID;

/**
 * 레시피 생성 관련 크레딧 포트.
 *
 * <p>레시피 생성 시 크레딧 지급 및 환불을 처리합니다.</p>
 */
public interface RecipeCreditPort {
    /**
     * 레시피 생성 크레딧 지급.
     *
     * @param userId 사용자 ID
     * @param recipeId 레시피 ID
     * @param creditCost 지급할 크레딧 양
     * @throws CreditException 크레딧 처리 실패 시
     */
    void spendRecipeCreate(UUID userId, UUID recipeId, long creditCost) throws CreditException;

    /**
     * 레시피 생성 크레딧 환불.
     *
     * @param userId 사용자 ID
     * @param recipeId 레시피 ID
     * @param creditCost 환불할 크레딧 양
     * @throws CreditException 크레딧 처리 실패 시
     */
    void refundRecipeCreate(UUID userId, UUID recipeId, long creditCost) throws CreditException;
}
