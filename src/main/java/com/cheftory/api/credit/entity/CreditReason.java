package com.cheftory.api.credit.entity;

/**
 * 크레딧 지급/사용 사유를 나타내는 열거형.
 */
public enum CreditReason {
    /** 회원가입 보너스 */
    SIGNUP_BONUS,
    /** 레시피 생성 */
    RECIPE_CREATE,
    /** 레시피 생성 환불 */
    RECIPE_CREATE_REFUND,
    /** 튜토리얼 완료 */
    TUTORIAL,
    /** 공유 */
    SHARE
}
