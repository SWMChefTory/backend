package com.cheftory.api.recipe.dto;

import com.cheftory.api.common.converter.Iso8601DurationToSecondConverter;
import lombok.Data;
import lombok.ToString;

import java.util.List;

@Data
@ToString
public class YoutubeVideoResponse {
    private List<Item> items;

    public String getThumbnailUri() {
        return items.getFirst()
                .getSnippet()
                .getThumbnails()
                .getMedium()
                .getUrl();
    }

    public String getTitle() {
        return items.getFirst()
                .getSnippet().getTitle();
    }

    public Long getSecondsDuration() {
        return Iso8601DurationToSecondConverter
                .convert(items
                        .getFirst()
                        .getContentDetails()
                        .getDuration());
    }

    @Data
    public static class Item {
        private Snippet snippet;
        private ContentDetails contentDetails;
    }

    @Data
    public static class Snippet {
        private String title;
        private Thumbnails thumbnails;
    }

    @Data
    public static class Thumbnails {
        private ThumbnailInfo defaultThumbnail;
        private ThumbnailInfo medium;
        private ThumbnailInfo high;

        // JSON 키가 "default"인 경우 Java에서 예약어라 아래와 같이 매핑
        @com.fasterxml.jackson.annotation.JsonProperty("default")
        public void setDefault(ThumbnailInfo defaultThumbnail) {
            this.defaultThumbnail = defaultThumbnail;
        }
    }

    @Data
    public static class ThumbnailInfo {
        private String url;
        private int width;
        private int height;
    }

    @Data
    public static class ContentDetails {
        private String duration; // ISO 8601 (예: PT3M33S)
    }
}

