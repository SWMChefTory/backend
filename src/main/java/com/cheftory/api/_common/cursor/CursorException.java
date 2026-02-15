package com.cheftory.api._common.cursor;

import com.cheftory.api.exception.CheftoryException;
import lombok.Getter;

@Getter
public class CursorException extends CheftoryException {

    /**
     * CursorErrorCode 생성
     *
     * @param errorCode 인증 에러 코드
     */
    public CursorException(CursorErrorCode errorCode) {
        super(errorCode);
    }

    public CursorException(CursorErrorCode errorCode, Throwable cause) {
        super(errorCode, cause);
    }
}
