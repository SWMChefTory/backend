package com.cheftory.api.recipe.ingredients.client.dto;

import com.cheftory.api.recipe.caption.client.dto.ClientCaptionResponse;
import com.cheftory.api.recipe.caption.dto.CaptionInfo;
import com.cheftory.api.recipe.caption.entity.LangCodeType;
import com.cheftory.api.recipe.ingredients.dto.ClientIngredientsResponse;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

@AllArgsConstructor(access= AccessLevel.PRIVATE)
@Builder(access=AccessLevel.PRIVATE)
@Getter
public class ClientIngredientsRequest {
    @JsonProperty("video_id")
    private String videoId;
    @JsonProperty("video_type")
    private String videoType;
    @JsonProperty("captions_data")
    private CaptionInfo captionInfo;

    public static ClientIngredientsRequest from(String videoId, String videoType, CaptionInfo captionInfo) {
        return ClientIngredientsRequest.builder()
                .videoId(videoId)
                .videoType(videoType)
                .captionInfo(captionInfo)
                .build();
    }
}
