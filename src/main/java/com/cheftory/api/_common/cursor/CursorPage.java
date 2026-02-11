package com.cheftory.api._common.cursor;

import java.util.List;

/**
 * 커서 기반 페이지네이션 결과를 담는 레코드.
 *
 * @param <T> 아이템 타입
 * @param items 페이지 아이템 목록
 * @param nextCursor 다음 페이지 조회를 위한 커서
 */
public record CursorPage<T>(List<T> items, String nextCursor) {
    /**
     * 다음 페이지가 존재하는지 확인합니다.
     *
     * @return 다음 페이지가 존재하면 true
     */
    public boolean hasNext() {
        return nextCursor != null && !nextCursor.isBlank();
    }

    /**
     * CursorPage 인스턴스를 생성합니다.
     *
     * @param items 페이지 아이템 목록
     * @param nextCursor 다음 페이지 조회를 위한 커서
     * @return CursorPage 인스턴스
     * @param <T> 아이템 타입
     */
    public static <T> CursorPage<T> of(List<T> items, String nextCursor) {
        return new CursorPage<>(items, nextCursor);
    }
}
