package com.cheftory.api.recipe.dto;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Builder(access= AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
public class RecipeOverviewsResponse {
    private List<RecipeOverview> recipeOverviews;
    public static RecipeOverviewsResponse of(List<RecipeOverview> recipeOverviews) {
        return RecipeOverviewsResponse
                .builder()
                .recipeOverviews(recipeOverviews)
                .build();
    }
}
