package com.cheftory.api._common.cursor;

/**
 * 커서 코덱 공통 지원 클래스.
 *
 * <p>입력 검증 및 예외 변환 정책을 공통화합니다.</p>
 */
public abstract class AbstractCursorCodecSupport {

    /**
     * 커서가 비어있지 않은지 검증합니다.
     *
     * @param cursor 커서 문자열
     * @throws CursorException 커서가 비어있는 경우
     */
    protected void requireNonBlank(String cursor) throws CursorException {
        if (cursor == null || cursor.isBlank()) {
            throw invalidCursor();
        }
    }

    /**
     * 구분자 위치가 문자열 중간인지 검증합니다.
     *
     * @param separatorIndex 구분자 인덱스
     * @param cursorLength 커서 길이
     * @throws CursorException 구분자 위치가 유효하지 않은 경우
     */
    protected void requireSeparatorInMiddle(int separatorIndex, int cursorLength) throws CursorException {
        if (separatorIndex <= 0 || separatorIndex == cursorLength - 1) {
            throw invalidCursor();
        }
    }

    /**
     * 공통 커서 예외를 생성합니다.
     *
     * @return INVALID_CURSOR 예외
     */
    protected CursorException invalidCursor() {
        return new CursorException(CursorErrorCode.INVALID_CURSOR);
    }

    /**
     * 원인 예외를 포함한 공통 커서 예외를 생성합니다.
     *
     * @param cause 원인 예외
     * @return INVALID_CURSOR 예외
     */
    protected CursorException invalidCursor(Throwable cause) {
        return new CursorException(CursorErrorCode.INVALID_CURSOR, cause);
    }
}
