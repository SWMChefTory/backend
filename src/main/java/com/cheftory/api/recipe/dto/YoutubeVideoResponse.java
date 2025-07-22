package com.cheftory.api.recipe.dto;

import com.cheftory.api.recipe.util.Iso8601DurationToSecondConverter;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record YoutubeVideoResponse(
    List<Item> items
) {
    public String getThumbnailUri() {
        return items.getFirst().snippet().thumbnails().medium().url();
    }

    public String getTitle() {
        return items.getFirst().snippet().title();
    }

    public Long getSecondsDuration() {
        return Iso8601DurationToSecondConverter.convert(
            items.getFirst().contentDetails().duration()
        );
    }

    public record Item(
        Snippet snippet,
        ContentDetails contentDetails
    ) {}

    public record Snippet(
        String title,
        Thumbnails thumbnails
    ) {}

    public record Thumbnails(
        @JsonProperty("default")
        ThumbnailInfo defaultThumbnail,
        ThumbnailInfo medium,
        ThumbnailInfo high
    ) {}

    public record ThumbnailInfo(
        String url,
        int width,
        int height
    ) {}

    public record ContentDetails(
        String duration  // ISO 8601 format (e.g., "PT3M33S")
    ) {}
}