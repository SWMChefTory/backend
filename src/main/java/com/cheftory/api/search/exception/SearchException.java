package com.cheftory.api.search.exception;

import com.cheftory.api.recipe.exception.RecipeException;

/**
 * 검색 관련 예외.
 *
 * <p>레시피 검색, 자동완성 등의 작업 중 오류가 발생한 경우 던져집니다.</p>
 */
public class SearchException extends RecipeException {

    /**
     * 생성자.
     *
     * @param errorCode 검색 에러 코드
     */
    public SearchException(SearchErrorCode errorCode) {
        super(errorCode);
    }

    public SearchException(SearchErrorCode errorCode, Throwable cause) {
        super(errorCode, cause);
    }
}
