package com.cheftory.api.recipe.caption.client.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@AllArgsConstructor(access= AccessLevel.PRIVATE)
@Builder(access=AccessLevel.PRIVATE)
@Getter
public class ClientCaptionRequest {
    @JsonProperty("video_id")
    private String videoId;
    @JsonProperty("video_type")
    private String videoType;

    public static ClientCaptionRequest from(String videoId, String videoType) {
        return ClientCaptionRequest.builder()
                .videoId(videoId)
                .videoType(videoType)
                .build();
    }
}
