package com.cheftory.api.recipe.info.dto;

import com.cheftory.api.recipe.info.entity.RecipeStatus;
import lombok.*;

import java.net.URI;

@AllArgsConstructor(access= AccessLevel.PRIVATE)
@Builder(access= AccessLevel.PRIVATE)
@Getter
public class RecipeInfoFindResponse {
    private RecipeStatus recipeStatus;
    private VideoInfo videoInfo;

    public static RecipeInfoFindResponse of(RecipeStatus recipeStatus, VideoInfo videoInfo) {
        return RecipeInfoFindResponse.builder()
                .recipeStatus(recipeStatus)
                .videoInfo(videoInfo)
                .build();
    }

}
