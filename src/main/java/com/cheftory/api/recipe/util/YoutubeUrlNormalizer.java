package com.cheftory.api.recipe.util;

import com.cheftory.api.recipe.exception.RecipeErrorCode;
import com.cheftory.api.recipe.exception.RecipeException;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.List;
import java.util.Objects;


@Component
public class YoutubeUrlNormalizer {

    public UriComponents normalize(UriComponents url) {
        String id = extractId(url);

        String normalizedPath = "https://www.youtube.com/watch";
        String normalizedQueryKey = "v";

        return UriComponentsBuilder
                .fromUriString(normalizedPath)
                .queryParam(normalizedQueryKey, id)
                .build();
    }

    private String extractId(UriComponents url) {
        if(Objects.isNull(url.getQuery())) {
            throw new RecipeException(RecipeErrorCode.YOUTUBE_URL_NULL);
        }
        String host = url.getHost();
        if(Objects.isNull(host)) {
            throw new RecipeException(RecipeErrorCode.YOUTUBE_URL_HOST_NULL);
        }
        if(host.equals("www.youtu.be")||host.equals("youtu.be")) {
            return extractIdFromSharedUrl(url);
        }

        if(host.equals("www.youtube.com")||host.equals("youtube.com")){
            return extractIdFromGeneralUrl(url);
        }

        throw new RecipeException(RecipeErrorCode.YOUTUBE_URL_HOST_INVALID);
    }

    private String extractIdFromSharedUrl(UriComponents url) {
        return url.getPathSegments().getFirst();
    }

    private String extractIdFromGeneralUrl(UriComponents url) {
        String path = url.getPath();
        if(Objects.isNull(path)) {
            throw new RecipeException(RecipeErrorCode.YOUTUBE_URL_PATH_NULL);
        }
        if (!path.equals("/watch")) {
            throw new RecipeException(RecipeErrorCode.YOUTUBE_URL_INVALID);
        }

        List<String> firstQueryValue = url
                .getQueryParams()
                .get("v");

        if(Objects.isNull(firstQueryValue)) {
            throw new RecipeException(RecipeErrorCode.YOUTUBE_URL_PATH_NULL);
        }

        if(firstQueryValue.isEmpty()){
            throw new RecipeException(RecipeErrorCode.YOUTUBE_URL_QUERY_PARAM_INVALID);
        }

        return firstQueryValue
                .getFirst();
    }

}
