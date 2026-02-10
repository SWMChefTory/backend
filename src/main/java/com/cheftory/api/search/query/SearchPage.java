package com.cheftory.api.search.query;

import java.util.List;
import java.util.UUID;

/**
 * 검색 페이지.
 *
 * <p>커서 기반 페이징을 사용한 검색 결과를 나타냅니다.</p>
 *
 * @param items 검색된 항목 ID 목록
 * @param nextCursor 다음 페이지 커서 (없으면 null)
 */
public record SearchPage(List<UUID> items, String nextCursor) {}
