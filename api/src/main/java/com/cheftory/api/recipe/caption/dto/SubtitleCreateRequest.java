package com.cheftory.api.recipe.caption.dto;

import com.cheftory.api.recipe.info.entity.RecipeInfo;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@AllArgsConstructor(access= AccessLevel.PRIVATE)
@Builder(access=AccessLevel.PRIVATE)
public class SubtitleCreateRequest {
    private String content;
    private RecipeInfo recipeInfo;

    public static SubtitleCreateRequest from(String content, RecipeInfo recipeInfo){
        return SubtitleCreateRequest.builder()
                .content(content)
                .recipeInfo(recipeInfo)
                .build();
    }
}
