package com.cheftory.api.recipe.content.briefing.client.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
public class BriefingClientRequest {
    @JsonProperty("video_id")
    private String videoId;

    public static BriefingClientRequest from(String videoId) {
        return new BriefingClientRequest(videoId);
    }
}
