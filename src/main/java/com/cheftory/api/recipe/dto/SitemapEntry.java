package com.cheftory.api.recipe.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * 사이트맵 항목 DTO
 *
 * @param recipeId 레시피 ID
 * @param updatedAt 수정 일시
 */
public record SitemapEntry(
        @JsonProperty("recipe_id") UUID recipeId,
        @JsonProperty("updated_at") LocalDateTime updatedAt) {
}
