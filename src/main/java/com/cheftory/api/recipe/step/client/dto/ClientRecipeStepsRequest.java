package com.cheftory.api.recipe.step.client.dto;

import com.cheftory.api.recipe.model.CaptionInfo;
import com.cheftory.api.recipe.ingredients.entity.Ingredient;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder(access = AccessLevel.PRIVATE)
@Getter
public class ClientRecipeStepsRequest {
    @JsonProperty("video_id")
    private String videoId;
    @JsonProperty("video_type")
    private String videoType;
    @JsonProperty("captions_data")
    private CaptionInfo captionInfo;
    private List<Ingredient> ingredients;

    public static ClientRecipeStepsRequest from(String videoId, String videoType, CaptionInfo captionInfo, List<Ingredient> ingredients) {
        return ClientRecipeStepsRequest.builder()
                .videoId(videoId)
                .videoType(videoType)
                .captionInfo(captionInfo)
                .ingredients(ingredients).build();
    }
}
