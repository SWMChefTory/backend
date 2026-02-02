package com.cheftory.api.recipe.dto;

import com.cheftory.api._common.cursor.CursorPage;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.net.URI;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record RecentRecipesResponse(
        @JsonProperty("recent_recipes") List<RecentRecipeResponse> recentRecipes,
        @JsonProperty("has_next") boolean hasNext,
        @JsonProperty("next_cursor") String nextCursor) {
    public static RecentRecipesResponse from(CursorPage<RecipeBookmarkOverview> recentRecipes) {
        List<RecentRecipeResponse> responses =
                recentRecipes.items().stream().map(RecentRecipeResponse::from).toList();
        return new RecentRecipesResponse(responses, recentRecipes.hasNext(), recentRecipes.nextCursor());
    }

    public record RecentRecipeResponse(
            @JsonProperty("viewed_at") LocalDateTime viewedAt,
            @JsonProperty("last_play_seconds") Integer lastPlaySeconds,
            @JsonProperty("recipe_id") UUID recipeId,
            @JsonProperty("recipe_title") String recipeTitle,
            @JsonProperty("video_thumbnail_url") URI thumbnailUrl,
            @JsonProperty("video_id") String videoId,
            @JsonProperty("channel_title") String channelTitle,
            @JsonProperty("description") String description,
            @JsonProperty("cook_time") Integer cookTime,
            @JsonProperty("servings") Integer servings,
            @JsonProperty("created_at") LocalDateTime createdAt,
            @JsonProperty("video_seconds") Integer videoSeconds,
            @JsonProperty("tags") List<Tag> tags,
            @JsonProperty("recipe_status") String recipeStatus,
            @JsonProperty("credit_cost") Long creditCost,
            @JsonProperty("video_type") String videoType) {
        public static RecentRecipeResponse from(RecipeBookmarkOverview info) {
            return new RecentRecipeResponse(
                    info.getViewedAt(),
                    info.getLastPlaySeconds(),
                    info.getRecipeId(),
                    info.getVideoTitle(),
                    info.getThumbnailUrl(),
                    info.getVideoId(),
                    info.getChannelTitle(),
                    info.getDescription(),
                    info.getCookTime(),
                    info.getServings(),
                    info.getRecipeCreatedAt(),
                    info.getVideoSeconds(),
                    info.getTags() != null
                            ? info.getTags().stream().map(Tag::from).toList()
                            : null,
                    info.getRecipeStatus().name(),
                    info.getCreditCost(),
                    info.getVideoType().name());
        }

        private record Tag(@JsonProperty("name") String name) {
            public static Tag from(String tag) {
                return new Tag(tag);
            }
        }
    }
}
