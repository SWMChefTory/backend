package com.cheftory.api.recipe.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

/**
 * 사이트맵 API 응답 래퍼
 *
 * @param entries 사이트맵 항목 목록
 * @param total 전체 공개 레시피 수
 */
public record PublicRecipeSitemapResponse(
        @JsonProperty("entries") List<SitemapEntry> entries,
        @JsonProperty("total") long total) {
}
