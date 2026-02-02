package com.cheftory.api.recipe.content.youtubemeta.client;

import com.cheftory.api.recipe.content.youtubemeta.exception.YoutubeMetaErrorCode;
import com.cheftory.api.recipe.content.youtubemeta.exception.YoutubeMetaException;
import com.cheftory.api.recipe.util.Iso8601DurationToSecondConverter;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public record YoutubeVideoResponse(List<Item> items) {

    public String getThumbnailUri() {
        Thumbnails thumbnails = items.getFirst().snippet().thumbnails();
        if (thumbnails == null) {
            throw new YoutubeMetaException(YoutubeMetaErrorCode.YOUTUBE_META_THUMBNAIL_NOT_FOUND);
        }

        ThumbnailInfo thumbnail = firstAvailableThumbnail(thumbnails);
        if (thumbnail == null) {
            throw new YoutubeMetaException(YoutubeMetaErrorCode.YOUTUBE_META_THUMBNAIL_NOT_FOUND);
        }

        return thumbnail.url();
    }

    private ThumbnailInfo firstAvailableThumbnail(Thumbnails thumbnails) {
        if (thumbnails.maxres() != null) {
            return thumbnails.maxres();
        }
        if (thumbnails.high() != null) {
            return thumbnails.high();
        }
        if (thumbnails.medium() != null) {
            return thumbnails.medium();
        }
        return thumbnails.defaultThumbnail();
    }

    public String getTitle() {
        return items.getFirst().snippet().title();
    }

    public Long getSecondsDuration() {
        String duration = items.getFirst().contentDetails().duration();
        if (duration == null) {
            return null;
        }
        return Iso8601DurationToSecondConverter.convert(duration);
    }

    public String getChannelId() {
        return items.getFirst().snippet().channelId();
    }

    public String getChannelTitle() {
        return items.getFirst().snippet().channelTitle();
    }

    public Boolean getEmbeddable() {
        return items.stream()
                .findFirst()
                .map(Item::status)
                .map(Status::embeddable)
                .orElse(null);
    }

    public record Item(Snippet snippet, ContentDetails contentDetails, Status status) {}

    public record Snippet(String title, String channelId, String channelTitle, Thumbnails thumbnails) {}

    public record Thumbnails(
            @JsonProperty("default") ThumbnailInfo defaultThumbnail,
            ThumbnailInfo medium,
            ThumbnailInfo high,
            ThumbnailInfo maxres) {}

    public record ThumbnailInfo(String url, int width, int height) {}

    public record ContentDetails(String duration) {}

    public record Status(Boolean embeddable) {}
}
