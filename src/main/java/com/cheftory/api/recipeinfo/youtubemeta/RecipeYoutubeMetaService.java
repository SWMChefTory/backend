package com.cheftory.api.recipeinfo.youtubemeta;

import com.cheftory.api._common.Clock;
import com.cheftory.api.recipeinfo.youtubemeta.client.VideoInfoClient;
import com.cheftory.api.recipeinfo.youtubemeta.exception.YoutubeMetaErrorCode;
import com.cheftory.api.recipeinfo.youtubemeta.exception.YoutubeMetaException;
import java.net.URI;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

@Service
@RequiredArgsConstructor
@Slf4j
public class RecipeYoutubeMetaService {

  private final RecipeYoutubeMetaRepository recipeYoutubeMetaRepository;
  private final YoutubeUrlNormalizer youtubeUrlNormalizer;
  private final VideoInfoClient videoInfoClient;
  private final Clock clock;

  public void create(YoutubeVideoInfo youtubeVideoInfo, UUID recipeId) {
    RecipeYoutubeMeta youtubeMeta = RecipeYoutubeMeta.create(youtubeVideoInfo, recipeId, clock);
    recipeYoutubeMetaRepository.save(youtubeMeta);
  }

  public void ban(UUID recipeId) {
    RecipeYoutubeMeta youtubeMeta =
        recipeYoutubeMetaRepository
            .findByRecipeId(recipeId)
            .orElseThrow(
                () -> new YoutubeMetaException(YoutubeMetaErrorCode.YOUTUBE_META_NOT_FOUND));
    youtubeMeta.ban();
    recipeYoutubeMetaRepository.save(youtubeMeta);
  }

  public List<RecipeYoutubeMeta> getByUrl(URI uri) {
    UriComponents uriOriginal = UriComponentsBuilder.fromUri(uri).build();
    UriComponents urlNormalized = youtubeUrlNormalizer.normalize(uriOriginal);
    List<RecipeYoutubeMeta> metas =
        recipeYoutubeMetaRepository.findAllByVideoUri(urlNormalized.toUri());
    if (metas.stream().anyMatch(RecipeYoutubeMeta::isBanned)) {
      throw new YoutubeMetaException(YoutubeMetaErrorCode.YOUTUBE_META_BANNED);
    }
    return metas;
  }

  public YoutubeVideoInfo getVideoInfo(URI uri) {
    UriComponents uriOriginal = UriComponentsBuilder.fromUri(uri).build();
    UriComponents urlNormalized = youtubeUrlNormalizer.normalize(uriOriginal);
    return videoInfoClient.fetchVideoInfo(urlNormalized);
  }

  public RecipeYoutubeMeta find(UUID recipeId) {
    return recipeYoutubeMetaRepository
        .findByRecipeId(recipeId)
        .orElseThrow(() -> new YoutubeMetaException(YoutubeMetaErrorCode.YOUTUBE_META_NOT_FOUND));
  }

  public List<RecipeYoutubeMeta> findsByRecipes(List<UUID> recipeIds) {
    return recipeYoutubeMetaRepository.findAllByRecipeIdIn(recipeIds);
  }
}
