package com.cheftory.api.recipe.dto;

import com.cheftory.api.recipe.entity.Recipe;
import com.cheftory.api.recipe.entity.VideoInfo;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@AllArgsConstructor(access= AccessLevel.PRIVATE)
@Builder(access=AccessLevel.PRIVATE)
@Getter
public class RecipeOverview {
    private VideoInfo videoInfo;
    private Integer count;
    public static RecipeOverview of(Recipe recipe) {
        return RecipeOverview.builder()
                .videoInfo(recipe.getVideoInfo())
                .count(recipe.getCount())
                .build();
    }
}
