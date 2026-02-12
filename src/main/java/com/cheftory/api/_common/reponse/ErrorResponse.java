package com.cheftory.api._common.reponse;

import com.cheftory.api.exception.CheftoryException;
import com.cheftory.api.exception.Error;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 에러 응답을 담는 DTO.
 */
@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class ErrorResponse {

    /** 에러 메시지 */
    private String message;
    /** 에러 코드 */
    private String errorCode;

    /**
     * Error 인터페이스로부터 ErrorResponse를 생성합니다.
     *
     * @param error 에러 정보
     * @return ErrorResponse 인스턴스
     */
    public static ErrorResponse of(Error error) {
        return new ErrorResponse(error.getMessage(), error.getErrorCode());
    }

    /**
     * CheftoryException으로부터 ErrorResponse를 생성합니다.
     *
     * @param exception CheftoryException 인스턴스
     * @return ErrorResponse 인스턴스
     */
    public static ErrorResponse from(CheftoryException exception) {
        return new ErrorResponse(
                exception.getError().getMessage(), exception.getError().getErrorCode());
    }
}
