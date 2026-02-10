package com.cheftory.api.credit.entity;

import com.cheftory.api._common.Clock;
import com.cheftory.api.credit.exception.CreditException;
import java.util.UUID;

/**
 * 크레딧 지급/사용 명령을 나타내는 레코드.
 *
 * @param userId 사용자 ID
 * @param amount 크레딧 금액
 * @param reason 크레딧 지급/사용 사유
 * @param idempotencyKey 멱등성 키
 */
public record Credit(UUID userId, long amount, CreditReason reason, String idempotencyKey) {

    /**
     * 회원가입 보너스 크레딧을 생성합니다.
     *
     * @param userId 사용자 ID
     * @return 회원가입 보너스 크레딧
     */
    public static Credit signupBonus(UUID userId) {
        return new Credit(userId, 100L, CreditReason.SIGNUP_BONUS, "signup-bonus:" + userId);
    }

    /**
     * 레시피 생성 비용 크레딧을 생성합니다.
     *
     * @param userId 사용자 ID
     * @param recipeId 레시피 ID
     * @param cost 소비할 크레딧 양
     * @return 레시피 생성 크레딧
     */
    public static Credit recipeCreate(UUID userId, UUID recipeId, long cost) {
        return new Credit(userId, cost, CreditReason.RECIPE_CREATE, "recipe-create:" + userId + ":" + recipeId);
    }

    /**
     * 레시피 생성 환불 크레딧을 생성합니다.
     *
     * @param userId 사용자 ID
     * @param recipeId 레시피 ID
     * @param amount 환불할 크레딧 양
     * @return 레시피 생성 환불 크레딧
     */
    public static Credit recipeCreateRefund(UUID userId, UUID recipeId, long amount) {
        return new Credit(
                userId, amount, CreditReason.RECIPE_CREATE_REFUND, "recipe-create-refund:" + userId + ":" + recipeId);
    }

    /**
     * 튜토리얼 완료 보너스 크레딧을 생성합니다.
     *
     * @param userId 사용자 ID
     * @return 튜토리얼 보너스 크레딧
     */
    public static Credit tutorial(UUID userId) {
        return new Credit(userId, 30L, CreditReason.TUTORIAL, "tutorial:" + userId);
    }

    /**
     * 공유 보너스 크레딧을 생성합니다.
     *
     * @param userId 사용자 ID
     * @param count 공유 횟수
     * @param clock 현재 시간 제공자
     * @return 공유 보너스 크레딧
     */
    public static Credit share(UUID userId, int count, Clock clock) {
        return new Credit(
                userId,
                10L,
                CreditReason.SHARE,
                "share:" + userId + ":" + clock.now().toLocalDate().toString() + ":" + count);
    }

    /**
     * 크레딧을 잔액에 지급합니다.
     *
     * @param balance 크레딧 잔액
     * @throws CreditException 크레딧 관련 예외 발생 시
     */
    public void grantTo(CreditUserBalance balance) throws CreditException {
        balance.apply(amount);
    }

    /**
     * 크레딧을 잔액에서 사용합니다.
     *
     * @param balance 크레딧 잔액
     * @throws CreditException 크레딧 관련 예외 발생 시
     */
    public void spendFrom(CreditUserBalance balance) throws CreditException {
        balance.apply(-amount);
    }
}
