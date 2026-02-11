package com.cheftory.api.exception;

import lombok.Getter;

/**
 * ChefTory 도메인의 일반적인 예외.
 *
 * <p>Error 인터페이스를 구현하는 에러 코드를 래핑합니다.</p>
 */
@Getter
public class CheftoryException extends Exception {
    private final Error error;

    /**
     * CheftoryException 생성자.
     *
     * @param error 에러 코드
     */
    public CheftoryException(Error error) {
        this.error = error;
    }
}
