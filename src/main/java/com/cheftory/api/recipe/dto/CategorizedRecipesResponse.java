package com.cheftory.api.recipe.dto;

import com.cheftory.api._common.cursor.CursorPage;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.net.URI;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Page;

public record CategorizedRecipesResponse(
        @JsonProperty("categorized_recipes") List<CategorizedRecipe> categorizedRecipes,
        @JsonProperty("current_page") Integer currentPage,
        @JsonProperty("total_pages") Integer totalPages,
        @JsonProperty("total_elements") Long totalElements,
        @JsonProperty("has_next") boolean hasNext,
        @JsonProperty("next_cursor") String nextCursor) {

    @Deprecated(forRemoval = true)
    public static CategorizedRecipesResponse from(Page<RecipeHistoryOverview> page) {
        List<CategorizedRecipe> responses =
                page.stream().map(CategorizedRecipe::from).toList();
        return new CategorizedRecipesResponse(
                responses, page.getNumber(), page.getTotalPages(), page.getTotalElements(), page.hasNext(), null);
    }

    public static CategorizedRecipesResponse from(CursorPage<RecipeHistoryOverview> slice) {
        List<CategorizedRecipe> responses =
                slice.items().stream().map(CategorizedRecipe::from).toList();
        return new CategorizedRecipesResponse(responses, null, null, null, slice.hasNext(), slice.nextCursor());
    }

    private record CategorizedRecipe(
            @JsonProperty("viewed_at") LocalDateTime viewedAt,
            @JsonProperty("last_play_seconds") Integer lastPlaySeconds,
            @JsonProperty("recipe_id") UUID recipeId,
            @JsonProperty("recipe_title") String recipeTitle,
            @JsonProperty("video_thumbnail_url") URI thumbnailUrl,
            @JsonProperty("video_id") String videoId,
            @JsonProperty("channel_title") String channelTitle,
            @JsonProperty("video_seconds") Integer videoSeconds,
            @JsonProperty("category_id") UUID categoryId,
            @JsonProperty("description") String description,
            @JsonProperty("cook_time") Integer cookTime,
            @JsonProperty("servings") Integer servings,
            @JsonProperty("created_at") LocalDateTime createdAt,
            @JsonProperty("tags") List<Tag> tags,
            @JsonProperty("credit_cost") Long creditCost) {
        public static CategorizedRecipe from(RecipeHistoryOverview info) {
            return new CategorizedRecipe(
                    info.getViewedAt(),
                    info.getLastPlaySeconds(),
                    info.getRecipeId(),
                    info.getVideoTitle(),
                    info.getThumbnailUrl(),
                    info.getVideoId(),
                    info.getChannelTitle(),
                    info.getVideoSeconds(),
                    info.getRecipeCategoryId(),
                    info.getDescription(),
                    info.getCookTime(),
                    info.getServings(),
                    info.getRecipeCreatedAt(),
                    info.getTags() != null
                            ? info.getTags().stream().map(Tag::from).toList()
                            : null,
                    info.getCreditCost());
        }
    }

    private record Tag(@JsonProperty("name") String name) {
        public static Tag from(String tag) {
            return new Tag(tag);
        }
    }
}
