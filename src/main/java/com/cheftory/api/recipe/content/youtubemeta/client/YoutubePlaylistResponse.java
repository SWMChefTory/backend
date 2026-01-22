package com.cheftory.api.recipe.content.youtubemeta.client;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public record YoutubePlaylistResponse(List<Item> items) {

    public boolean hasItems() {
        return items != null && !items.isEmpty();
    }

    public record Item(Snippet snippet) {}

    public record Snippet(@JsonProperty("videoId") String videoId) {}
}
