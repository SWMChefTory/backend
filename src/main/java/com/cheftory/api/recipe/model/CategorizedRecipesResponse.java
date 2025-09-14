package com.cheftory.api.recipe.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.net.URI;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Page;

public record CategorizedRecipesResponse(
    @JsonProperty("categorized_recipes")
    List<CategorizedRecipe> categorizedRecipes,

    @JsonProperty("current_page")
    int currentPage,

    @JsonProperty("total_pages")
    int totalPages,

    @JsonProperty("total_elements")
    long totalElements,

    @JsonProperty("has_next")
    boolean hasNext
) {
    public static CategorizedRecipesResponse from(Page<RecipeHistory> categorizedRecipes) {

        List<CategorizedRecipe> responses = categorizedRecipes.stream()
            .map(CategorizedRecipe::from)
            .toList();
        return new CategorizedRecipesResponse(
            responses,
            categorizedRecipes.getNumber(),
            categorizedRecipes.getTotalPages(),
            categorizedRecipes.getTotalElements(),
            categorizedRecipes.hasNext());
    }

    private record CategorizedRecipe(
        @JsonProperty("viewed_at")
        LocalDateTime viewedAt,

        @JsonProperty("last_play_seconds")
        Integer lastPlaySeconds,

        @JsonProperty("recipe_id")
        UUID recipeId,

        @JsonProperty("recipe_title")
        String recipeTitle,

        @JsonProperty("video_thumbnail_url")
        URI thumbnailUrl,

        @JsonProperty("video_id")
        String videoId,

        @JsonProperty("video_seconds")
        Integer videoSeconds,
        @JsonProperty("category_id")
        UUID categoryId
    ){
        public static CategorizedRecipe from(RecipeHistory info) {
            return new CategorizedRecipe(
                info.getRecipeViewStatus().getViewedAt(),
                info.getRecipeViewStatus().getLastPlaySeconds(),
                info.getRecipe().getId(),
                info.getRecipe().getVideoInfo().getTitle(),
                info.getRecipe().getVideoInfo().getThumbnailUrl(),
                info.getRecipe().getVideoInfo().getVideoId(),
                info.getRecipe().getVideoInfo().getVideoSeconds(),
                info.getRecipeViewStatus().getRecipeCategoryId()
            );
        }
    }
}
