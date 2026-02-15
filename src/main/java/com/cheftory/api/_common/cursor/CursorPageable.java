package com.cheftory.api._common.cursor;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

/**
 * 커서 기반 페이지네이션을 위한 유틸리티 클래스.
 */
public final class CursorPageable {
    /** 커서 페이지 크기 */
    private static final int CURSOR_PAGE_SIZE = 20;

    /**
     * 기본 생성자 (private).
     */
    private CursorPageable() {}

    /**
     * 첫 번째 페이지를 위한 Pageable을 생성합니다.
     *
     * @return 첫 번째 페이지 Pageable
     */
    public static Pageable firstPage() {
        return PageRequest.of(0, CURSOR_PAGE_SIZE);
    }

    /**
     * 프로브 Pageable을 생성합니다.
     *
     * @param pageable 기존 Pageable
     * @return 프로브 Pageable
     */
    public static Pageable probe(Pageable pageable) {
        return PageRequest.of(pageable.getPageNumber(), pageable.getPageSize() + 1, pageable.getSort());
    }
}
