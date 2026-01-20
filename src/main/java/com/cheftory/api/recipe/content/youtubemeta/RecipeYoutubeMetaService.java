package com.cheftory.api.recipe.content.youtubemeta;

import com.cheftory.api._common.Clock;
import com.cheftory.api.recipe.content.youtubemeta.client.VideoInfoClient;
import com.cheftory.api.recipe.content.youtubemeta.entity.RecipeYoutubeMeta;
import com.cheftory.api.recipe.content.youtubemeta.entity.YoutubeUri;
import com.cheftory.api.recipe.content.youtubemeta.entity.YoutubeVideoInfo;
import com.cheftory.api.recipe.content.youtubemeta.exception.YoutubeMetaErrorCode;
import com.cheftory.api.recipe.content.youtubemeta.exception.YoutubeMetaException;
import java.net.URI;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class RecipeYoutubeMetaService {

    private final RecipeYoutubeMetaRepository recipeYoutubeMetaRepository;
    private final VideoInfoClient videoInfoClient;
    private final Clock clock;

    public void create(YoutubeVideoInfo youtubeVideoInfo, UUID recipeId) {
        RecipeYoutubeMeta youtubeMeta = RecipeYoutubeMeta.create(youtubeVideoInfo, recipeId, clock);
        recipeYoutubeMetaRepository.save(youtubeMeta);
    }

    public void ban(UUID recipeId) {
        RecipeYoutubeMeta youtubeMeta = recipeYoutubeMetaRepository
                .findByRecipeId(recipeId)
                .orElseThrow(() -> new YoutubeMetaException(YoutubeMetaErrorCode.YOUTUBE_META_NOT_FOUND));
        youtubeMeta.ban();
        recipeYoutubeMetaRepository.save(youtubeMeta);
    }

    public List<RecipeYoutubeMeta> getByUrl(URI uri) {
        YoutubeUri youtubeUri = YoutubeUri.from(uri);
        List<RecipeYoutubeMeta> metas = recipeYoutubeMetaRepository.findAllByVideoUri(youtubeUri.getNormalizedUrl());
        validateAllActive(metas);
        return metas;
    }

    public YoutubeVideoInfo getVideoInfo(URI uri) {
        YoutubeUri youtubeUri = YoutubeUri.from(uri);
        return videoInfoClient.fetchVideoInfo(youtubeUri);
    }

    public RecipeYoutubeMeta get(UUID recipeId) {
        return recipeYoutubeMetaRepository
                .findByRecipeId(recipeId)
                .orElseThrow(() -> new YoutubeMetaException(YoutubeMetaErrorCode.YOUTUBE_META_NOT_FOUND));
    }

    public List<RecipeYoutubeMeta> getByRecipes(List<UUID> recipeIds) {
        return recipeYoutubeMetaRepository.findAllByRecipeIdIn(recipeIds);
    }

    public void block(UUID recipeId) {
        RecipeYoutubeMeta youtubeMeta = recipeYoutubeMetaRepository
                .findByRecipeId(recipeId)
                .orElseThrow(() -> new YoutubeMetaException(YoutubeMetaErrorCode.YOUTUBE_META_NOT_FOUND));
        YoutubeUri youtubeUri = YoutubeUri.from(youtubeMeta.getVideoUri());
        if (videoInfoClient.isBlockedVideo(youtubeUri)) {
            youtubeMeta.block();
            recipeYoutubeMetaRepository.save(youtubeMeta);
        } else {
            throw new YoutubeMetaException(YoutubeMetaErrorCode.YOUTUBE_META_NOT_BLOCKED_VIDEO);
        }
    }

    private void validateAllActive(List<RecipeYoutubeMeta> metas) {
        if (metas.stream().anyMatch(RecipeYoutubeMeta::isBanned)) {
            throw new YoutubeMetaException(YoutubeMetaErrorCode.YOUTUBE_META_BANNED);
        }
        if (metas.stream().anyMatch(RecipeYoutubeMeta::isBlocked)) {
            throw new YoutubeMetaException(YoutubeMetaErrorCode.YOUTUBE_META_BLOCKED);
        }
    }
}
