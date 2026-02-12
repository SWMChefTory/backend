package com.cheftory.api._common.cursor;

/**
 * 커서를 인코딩/디코딩하는 인터페이스.
 *
 * @param <T> 커서 타입
 */
public interface CursorCodec<T> {
    /**
     * 커서 문자열을 디코딩합니다.
     *
     * @param cursor 커서 문자열
     * @return 디코딩된 커서 객체
     * @throws CursorException 커서 디코딩 실패 시
     */
    T decode(String cursor) throws CursorException;

    /**
     * 커서 객체를 인코딩합니다.
     *
     * @param value 커서 객체
     * @return 인코딩된 커서 문자열
     * @throws CursorException 커서 인코딩 실패 시
     */
    String encode(T value) throws CursorException;
}
