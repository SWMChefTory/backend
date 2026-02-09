package com.cheftory.api.recipe.content.youtubemeta.entity;

import com.cheftory.api.recipe.content.youtubemeta.exception.YoutubeMetaErrorCode;
import com.cheftory.api.recipe.content.youtubemeta.exception.YoutubeMetaException;
import java.net.URI;
import java.util.List;
import java.util.Objects;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
@NoArgsConstructor
public class YoutubeUri {

    private String videoId;
    private URI normalizedUrl;

    public static YoutubeUri from(URI uri) throws YoutubeMetaException {
        UriComponents u = UriComponentsBuilder.fromUri(uri).build();
        String id = extractId(u);

        String normalizedPath = "https://www.youtube.com/watch";
        String normalizedQueryKey = "v";

        URI normalized = UriComponentsBuilder.fromUriString(normalizedPath)
                .queryParam(normalizedQueryKey, id)
                .build()
                .toUri();

        return new YoutubeUri(id, normalized);
    }

    private static String extractId(UriComponents url) throws YoutubeMetaException {
        String host = url.getHost();
        if (Objects.isNull(host) || host.isBlank()) {
            throw new YoutubeMetaException(YoutubeMetaErrorCode.YOUTUBE_URL_HOST_NULL);
        }
        if (host.equals("www.youtu.be") || host.equals("youtu.be")) {
            return extractIdFromSharedUrl(url);
        }

        if (host.equals("www.youtube.com") || host.equals("youtube.com")) {
            return extractIdFromGeneralUrl(url);
        }
        throw new YoutubeMetaException(YoutubeMetaErrorCode.YOUTUBE_URL_HOST_INVALID);
    }

    private static String extractIdFromSharedUrl(UriComponents url) throws YoutubeMetaException {
        List<String> pathSegments = url.getPathSegments();

        if (pathSegments.isEmpty()) {
            throw new YoutubeMetaException(YoutubeMetaErrorCode.YOUTUBE_URL_PATH_NULL);
        }

        String videoId = pathSegments.getFirst();
        if (Objects.isNull(videoId) || videoId.isBlank()) {
            throw new YoutubeMetaException(YoutubeMetaErrorCode.YOUTUBE_URL_PATH_INVALID);
        }

        return videoId;
    }

    private static String extractIdFromGeneralUrl(UriComponents url) throws YoutubeMetaException {
        String path = url.getPath();
        if (Objects.isNull(path) || path.isBlank()) {
            throw new YoutubeMetaException(YoutubeMetaErrorCode.YOUTUBE_URL_PATH_NULL);
        }
        if (Objects.isNull(url.getQuery())) {
            throw new YoutubeMetaException(YoutubeMetaErrorCode.YOUTUBE_URL_QUERY_PARAM_INVALID);
        }
        if (!path.equals("/watch")) {
            throw new YoutubeMetaException(YoutubeMetaErrorCode.YOUTUBE_URL_PATH_INVALID);
        }

        List<String> firstQueryValue = url.getQueryParams().get("v");

        if (Objects.isNull(firstQueryValue) || firstQueryValue.isEmpty()) {
            throw new YoutubeMetaException(YoutubeMetaErrorCode.YOUTUBE_URL_QUERY_PARAM_INVALID);
        }

        String videoId = firstQueryValue.getFirst();
        if (Objects.isNull(videoId) || videoId.isBlank()) {
            throw new YoutubeMetaException(YoutubeMetaErrorCode.YOUTUBE_URL_QUERY_PARAM_INVALID);
        }

        return videoId;
    }
}
