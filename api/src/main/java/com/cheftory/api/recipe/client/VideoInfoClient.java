package com.cheftory.api.recipe.client;

import com.cheftory.api.recipe.dto.YoutubeVideoResponse;
import com.cheftory.api.recipe.entity.VideoInfo;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponents;

import java.net.URI;
import java.util.Objects;

@Component
public class VideoInfoClient {
    public VideoInfoClient(@Qualifier("youtubeClient")WebClient webClient){
        this.webClient = webClient;
    }
    private final WebClient webClient;

    @Value("${youtube.api-token}")
    private String YOUTUBE_KEY;

    public VideoInfo fetchVideoInfo(UriComponents url) {
        String videoId = url
                .getQueryParams()
                .getFirst("v");

        YoutubeVideoResponse youtubeVideoResponse = webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/videos")
                        .queryParam("id",videoId)
                        .queryParam("key",YOUTUBE_KEY)
                        .queryParam("part", "snippet,contentDetails")
                        .build())
                .retrieve()
                .bodyToMono(YoutubeVideoResponse.class)
                .block(); //recipeMetaClient

        Objects.requireNonNull(youtubeVideoResponse,"비디오 응답이 null 입니다.");

        //검증로직 필요하겠는데... 그리고 여기서 바로 객체 뱉는거 별로같은데
        return VideoInfo.from(
                url
                ,youtubeVideoResponse.getTitle()
                ,URI.create(youtubeVideoResponse.getThumbnailUri())
                ,youtubeVideoResponse.getSecondsDuration().intValue());
    }
}
