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
        super(error.getMessage());
        this.error = error;
    }

    /**
     * 원인 예외를 포함한 CheftoryException 생성자.
     *
     * @param error 에러 코드
     * @param cause 원인 예외
     */
    public CheftoryException(Error error, Throwable cause) {
        super(error.getMessage(), cause);
        this.error = error;
    }
}
