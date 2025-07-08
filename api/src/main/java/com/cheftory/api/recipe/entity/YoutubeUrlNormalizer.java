package com.cheftory.api.recipe.entity;

import org.springframework.stereotype.Component;
import org.springframework.util.Assert;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.List;
import java.util.Objects;


@Component
public class YoutubeUrlNormalizer {

    public UriComponents normalize(UriComponents url){
        Objects.requireNonNull(url, "url must not be null");

        String id;
        System.out.println(url.getHost()+url.getPath()+"!!");
        if((url.getHost()+url.getPath()).equals("www.youtube.com/watch")){
            id= tryGetIdFromUrl(url);
        }
        else if(url.getHost().equals("www.youtu.be")){
            id = tryGetIdFromUrl(url);
        }
        else{
            throw new IllegalStateException("유튜브 url이 아닙니다.");
        }

        return UriComponentsBuilder
                .fromUriString("https://youtube.com/watch")
                .queryParam("v",id)
                .build();
    }

    private String tryGetIdFromSharedUrl(UriComponents url){
        List<String> pathSegments = url.getPathSegments();

        Assert.isTrue(pathSegments.size()==1,"공유하기 url 형식이 아닙니다.");

        return pathSegments.getFirst();
    }

    private String tryGetIdFromUrl(UriComponents url){
        List<String> firstQueryValue = url.getQueryParams().get("v");

        Assert.notNull(firstQueryValue, "유튜브 영상 id가 존재하지 않습니다.");
        Assert.noNullElements(firstQueryValue,"유튜브 영상 id에 값이 존재하지 않습니다.");

        return firstQueryValue.getFirst();
    }
}
