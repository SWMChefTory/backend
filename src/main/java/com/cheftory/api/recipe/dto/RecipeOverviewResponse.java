package com.cheftory.api.recipe.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.net.URI;
import java.util.List;

public record RecipeOverviewResponse(
        @JsonProperty("recipe_id") String recipeId,
        @JsonProperty("recipe_title") String recipeTitle,
        @JsonProperty("tags") List<Tag> tags,
        @JsonProperty("is_viewed") Boolean isViewed,
        @JsonProperty("description") String description,
        @JsonProperty("servings") Integer servings,
        @JsonProperty("cooking_time") Integer cookingTime,
        @JsonProperty("video_id") String videoId,
        @JsonProperty("channel_title") String channelTitle,
        @JsonProperty("count") Integer count,
        @JsonProperty("video_url") String videoUrl,
        @JsonProperty("video_type") String videoType,
        @JsonProperty("video_thumbnail_url") URI thumbnailUrl,
        @JsonProperty("video_seconds") Integer videoSeconds,
        @JsonProperty("credit_cost") Long creditCost) {

    public static RecipeOverviewResponse of(RecipeOverview recipe) {
        return new RecipeOverviewResponse(
                recipe.getRecipeId().toString(),
                recipe.getVideoTitle(),
                recipe.getTags().stream().map(Tag::new).toList(),
                recipe.getIsViewed(),
                recipe.getDescription(),
                recipe.getServings(),
                recipe.getCookTime(),
                recipe.getVideoId(),
                recipe.getChannelTitle(),
                recipe.getViewCount(),
                recipe.getVideoUri().toString(),
                recipe.getVideoType().name(),
                recipe.getThumbnailUrl(),
                recipe.getVideoSeconds(),
                recipe.getCreditCost());
    }

    private record Tag(@JsonProperty("name") String name) {}
}
