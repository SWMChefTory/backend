package com.cheftory.api.exception;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

/**
 * Error를 HTTP 상태코드로 변환합니다.
 */
@Component
public class ErrorStatusResolver {

    /**
     * 에러 타입을 HTTP 상태코드로 변환합니다.
     *
     * @param error 에러
     * @return HTTP 상태코드
     */
    public HttpStatus resolve(Error error) {
        if (error == null) return HttpStatus.INTERNAL_SERVER_ERROR;

        return switch (error.getType()) {
            case NOT_FOUND -> HttpStatus.NOT_FOUND;
            case VALIDATION -> HttpStatus.BAD_REQUEST;
            case CONFLICT -> HttpStatus.CONFLICT;
            case UNAUTHORIZED -> HttpStatus.UNAUTHORIZED;
            case FORBIDDEN -> HttpStatus.FORBIDDEN;
            case INTERNAL -> HttpStatus.INTERNAL_SERVER_ERROR;
        };
    }
}
