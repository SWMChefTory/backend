package com.cheftory.api.affiliate.coupang.exception;

import com.cheftory.api.recipe.exception.RecipeException;

/**
 * 쿠팡 파트너스 API 관련 예외.
 *
 * <p>쿠팡 API 요청 실패 시 발생하는 예외입니다.</p>
 */
public class CoupangException extends RecipeException {

    /**
     * 쿠팡 에러 코드를 사용하여 예외를 생성합니다.
     *
     * @param e 쿠팡 에러 코드
     */
    public CoupangException(CoupangErrorCode e) {
        super(e);
    }
}
