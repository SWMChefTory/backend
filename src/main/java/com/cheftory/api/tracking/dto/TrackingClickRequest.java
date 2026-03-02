package com.cheftory.api.tracking.dto;

import com.cheftory.api.tracking.entity.SurfaceType;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

/**
 * 레시피 클릭 단건 기록 요청 DTO
 *
 * @param requestId 리스트 로드 식별자
 * @param surfaceType 클릭 발생 위치
 * @param recipeId 클릭된 레시피 ID
 * @param position 리스트 내 순서 (0-based)
 * @param timestamp 클릭 시각 (Unix ms)
 */
public record TrackingClickRequest(
        @JsonProperty("request_id") @NotNull UUID requestId,
        @JsonProperty("surface_type") @NotNull SurfaceType surfaceType,
        @JsonProperty("recipe_id") @NotNull UUID recipeId,
        @JsonProperty("position") @NotNull Integer position,
        @JsonProperty("timestamp") @NotNull Long timestamp) {}
