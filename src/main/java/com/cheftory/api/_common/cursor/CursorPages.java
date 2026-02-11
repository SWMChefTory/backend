package com.cheftory.api._common.cursor;

import java.util.List;
import java.util.function.Function;

/**
 * 커서 기반 페이지네이션 유틸리티 클래스.
 */
public final class CursorPages {
    /**
     * 기본 생성자 (private).
     */
    private CursorPages() {}

    /**
     * 목록을 커서 페이지로 변환합니다.
     *
     * @param rows 전체 데이터 행 목록
     * @param limit 최대 아이템 개수
     * @param cursorOf 아이템에서 커서를 추출하는 함수
     * @return CursorPage 인스턴스
     * @param <T> 아이템 타입
     */
    public static <T> CursorPage<T> of(List<T> rows, int limit, Function<T, String> cursorOf) {
        boolean hasNext = rows.size() > limit;
        List<T> items = hasNext ? rows.subList(0, limit) : rows;

        String nextCursor = hasNext && !items.isEmpty() ? cursorOf.apply(items.getLast()) : null;

        return CursorPage.of(items, nextCursor);
    }
}
