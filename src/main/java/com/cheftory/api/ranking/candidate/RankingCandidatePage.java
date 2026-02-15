package com.cheftory.api.ranking.candidate;

import java.util.List;
import java.util.UUID;

/**
 * 랭킹 후보 페이지.
 *
 * <p>랭킹 도메인에서 사용하는 후보군 조회 결과를 나타냅니다.</p>
 *
 * @param items 후보 아이템 ID 목록
 * @param nextCursor 다음 페이지 커서
 */
public record RankingCandidatePage(List<UUID> items, String nextCursor) {}
