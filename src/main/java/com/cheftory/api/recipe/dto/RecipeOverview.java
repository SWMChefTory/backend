package com.cheftory.api.recipe.dto;

import com.cheftory.api.recipe.entity.Recipe;
import com.cheftory.api.recipe.entity.VideoInfo;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.UUID;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder(access = AccessLevel.PRIVATE)
@Getter
public class RecipeOverview {
    private UUID id;
    private VideoInfo videoInfo;
    private Integer count;

    public static RecipeOverview from(Recipe recipe) {
        return RecipeOverview.builder()
                .id(recipe.getId())
                .videoInfo(recipe.getVideoInfo())
                .count(recipe.getCount())
                .build();
    }
}
