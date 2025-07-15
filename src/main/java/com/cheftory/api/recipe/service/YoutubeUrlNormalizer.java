package com.cheftory.api.recipe.service;

import org.springframework.stereotype.Component;
import org.springframework.util.Assert;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.List;
import java.util.Objects;


@Component
public class YoutubeUrlNormalizer {
    private final String notExpectedHostMessage = "기대하는 호스트가 아닙니다.";
    private final String notExpectedQueryParameter = "기대하는 쿼리 파라미터가 아닙니다.";

    private final String nullUrlErrorMessage = "URL이 비어있습니다.";
    private final String nullHostErrorMessage = "호스트가 비어있습니다.";
    private final String nullPathErrorMessage = "경로가 비어있습니다.";

    public UriComponents normalize(UriComponents url) {
        String id = extractId(url);

        String normalizedPath = "https://youtube.com/watch";
        String normalizedQueryKey = "v";

        return UriComponentsBuilder
                .fromUriString(normalizedPath)
                .queryParam(normalizedQueryKey, id)
                .build();
    }

    private String extractId(UriComponents url) {
        Assert.notNull(url, nullUrlErrorMessage);
        String host = url.getHost();
        Assert.notNull(host, nullHostErrorMessage);
        if(host.equals("www.youtu.be")){
            return extractIdFromSharedUrl(url);
        }

        if(host.equals("www.youtube.com")){
            return extractIdFromGeneralUrl(url);
        }

        throw new IllegalArgumentException(notExpectedHostMessage);
    }

    private String extractIdFromSharedUrl(UriComponents url) {
        return url.getPathSegments().getFirst();
    }

    private String extractIdFromGeneralUrl(UriComponents url) {
        String path = url.getPath();
        Assert.notNull(path, nullPathErrorMessage);
        if (!path.equals("/watch")) {
            throw new IllegalArgumentException("잘못된 Path입니다.");
        }

        List<String> firstQueryValue = url
                .getQueryParams()
                .get("v");

        Assert.notNull(firstQueryValue, notExpectedQueryParameter);
        Assert.noNullElements(firstQueryValue, notExpectedQueryParameter);

        return firstQueryValue
                .getFirst();
    }

}
