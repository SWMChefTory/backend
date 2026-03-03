package com.cheftory.api.tracking.dto;

import com.cheftory.api.tracking.entity.SurfaceType;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.List;
import java.util.UUID;

/**
 * 레시피 노출 배치 기록 요청 DTO
 *
 * @param requestId 리스트 로드 식별자
 * @param surfaceType 노출 위치
 * @param impressions 노출된 레시피 목록 (내부 요소 cascading validation)
 */
public record TrackingImpressionRequest(
        @JsonProperty("request_id") @NotNull UUID requestId,
        @JsonProperty("surface_type") @NotNull SurfaceType surfaceType,

        @JsonProperty("impressions") @NotNull @Size(min = 1) @Valid
        List<ImpressionItem> impressions) {

    /**
     * 개별 노출 항목
     *
     * @param recipeId 노출된 레시피 ID
     * @param position 리스트 내 순서 (0-based)
     * @param timestamp 뷰포트 진입 시각 (Unix ms)
     */
    public record ImpressionItem(
            @JsonProperty("recipe_id") @NotNull UUID recipeId,
            @JsonProperty("position") @NotNull Integer position,
            @JsonProperty("timestamp") @NotNull Long timestamp) {}
}
