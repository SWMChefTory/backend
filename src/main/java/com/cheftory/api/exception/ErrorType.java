package com.cheftory.api.exception;

/**
 * 에러의 의미 분류.
 *
 * <p>HTTP 계층과 분리된 도메인 의미 타입입니다.</p>
 */
public enum ErrorType {
    NOT_FOUND,
    VALIDATION,
    CONFLICT,
    UNAUTHORIZED,
    FORBIDDEN,
    INTERNAL
}
