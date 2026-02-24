package com.cheftory.api.recipe.content.youtubemeta.client;

import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.HttpExchange;

@HttpExchange
public interface YoutubeMetaHttpApi {

    @GetExchange("/videos")
    YoutubeVideoResponse fetchVideo(
            @RequestParam("id") String videoId, @RequestParam("key") String key, @RequestParam("part") String part);

    @GetExchange("/playlistItems")
    YoutubePlaylistResponse fetchPlaylistItems(
            @RequestParam("part") String part,
            @RequestParam("playlistId") String playlistId,
            @RequestParam("videoId") String videoId,
            @RequestParam("key") String key);
}
