package com.cheftory.api.recipe.info.client;

import com.cheftory.api.recipe.dto.YoutubeVideoResponse;
import com.cheftory.api.recipe.info.entity.RecipeInfo;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponents;

import java.net.URI;

@Component
public class RecipeInfoClient {
    public RecipeInfoClient(@Qualifier("youtubeClient")WebClient webClient){
        this.webClient = webClient;
    }
    private final WebClient webClient;

    @Value("${youtube.api-token}")
    private String YOUTUBE_KEY;

    public RecipeInfo fetchRecipeInfo(UriComponents url) {
        String youtubeId = url
                .getQueryParams()
                .getFirst("v");

        YoutubeVideoResponse youtubeVideoResponse = webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/videos")
                        .queryParam("id",youtubeId)
                        .queryParam("key",YOUTUBE_KEY)
                        .queryParam("part", "snippet,contentDetails")
                        .build())
                .retrieve()
                .bodyToMono(YoutubeVideoResponse.class)
                .block(); //recipeMetaClient

        System.out.println(youtubeVideoResponse+"!!!!!!!!!!!!!!!!!!!!!1");
        //검증로직 필요하겠는데... 그리고 여기서 바로 객체 뱉는거 별로같은데
        RecipeInfo recipeInfo = RecipeInfo.preCreationOf(
                url.toUri(),
                youtubeVideoResponse.getTitle()
                ,URI.create(youtubeVideoResponse.getThumbnailUri())
                ,youtubeVideoResponse.getSecondsDuration()
        );

        return recipeInfo;
    }
}
