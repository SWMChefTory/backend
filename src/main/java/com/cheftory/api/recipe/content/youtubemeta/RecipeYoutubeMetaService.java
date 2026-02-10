package com.cheftory.api.recipe.content.youtubemeta;

import com.cheftory.api._common.Clock;
import com.cheftory.api.recipe.content.youtubemeta.client.YoutubeMetaClient;
import com.cheftory.api.recipe.content.youtubemeta.entity.RecipeYoutubeMeta;
import com.cheftory.api.recipe.content.youtubemeta.entity.YoutubeMetaStatus;
import com.cheftory.api.recipe.content.youtubemeta.entity.YoutubeUri;
import com.cheftory.api.recipe.content.youtubemeta.entity.YoutubeVideoInfo;
import com.cheftory.api.recipe.content.youtubemeta.exception.YoutubeMetaErrorCode;
import com.cheftory.api.recipe.content.youtubemeta.exception.YoutubeMetaException;
import com.cheftory.api.recipe.content.youtubemeta.repository.RecipeYoutubeMetaRepository;
import java.net.URI;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 레시피 유튜브 메타 도메인의 비즈니스 로직을 처리하는 서비스
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class RecipeYoutubeMetaService {

    private final RecipeYoutubeMetaRepository repository;
    private final YoutubeMetaClient client;
    private final Clock clock;

    /**
     * 유튜브 메타 정보 생성
     *
     * @param youtubeVideoInfo 유튜브 비디오 정보
     * @param recipeId 레시피 ID
     */
    public void create(YoutubeVideoInfo youtubeVideoInfo, UUID recipeId) {
        RecipeYoutubeMeta youtubeMeta = RecipeYoutubeMeta.create(youtubeVideoInfo, recipeId, clock);
        repository.create(youtubeMeta);
    }

    /**
     * 레시피 유튜브 메타 정보 차단 (BANNED)
     *
     * @param recipeId 레시피 ID
     * @throws YoutubeMetaException 메타 정보를 찾을 수 없을 때
     */
    public void ban(UUID recipeId) throws YoutubeMetaException {
        repository.ban(recipeId);
    }

    /**
     * 레시피 유튜브 메타 정보 실패 처리
     *
     * @param recipeId 레시피 ID
     */
    public void failed(UUID recipeId) {
        repository.failed(recipeId);
    }

    /**
     * URL로 유튜브 메타 정보 조회
     *
     * @param uri 유튜브 URL
     * @return 유튜브 메타 정보
     * @throws YoutubeMetaException 메타 정보를 찾을 수 없거나 차단된 경우
     */
    public RecipeYoutubeMeta getByUrl(URI uri) throws YoutubeMetaException {
        YoutubeUri youtubeUri = YoutubeUri.from(uri);
        List<RecipeYoutubeMeta> metas = repository.find(youtubeUri.getNormalizedUrl());

        validateAllActive(metas);

        List<RecipeYoutubeMeta> actives = metas.stream()
                .filter(m -> m.getStatus() == YoutubeMetaStatus.ACTIVE)
                .toList();

        if (actives.isEmpty()) {
            throw new YoutubeMetaException(YoutubeMetaErrorCode.YOUTUBE_META_NOT_FOUND);
        }

        if (actives.size() > 1) {
            log.warn(
                    "Multiple RecipeYoutubeMeta detected. count={}, recipeIds={}",
                    metas.size(),
                    metas.stream().map(RecipeYoutubeMeta::getRecipeId).toList());
        }

        return actives.getFirst();
    }

    /**
     * 외부 API를 통해 유튜브 비디오 정보 조회
     *
     * @param uri 유튜브 URL
     * @return 유튜브 비디오 정보
     * @throws YoutubeMetaException API 호출 실패 시
     */
    public YoutubeVideoInfo getVideoInfo(URI uri) throws YoutubeMetaException {
        YoutubeUri youtubeUri = YoutubeUri.from(uri);
        return client.fetch(youtubeUri);
    }

    /**
     * 레시피 ID로 유튜브 메타 정보 조회
     *
     * @param recipeId 레시피 ID
     * @return 유튜브 메타 정보
     * @throws YoutubeMetaException 메타 정보를 찾을 수 없을 때
     */
    public RecipeYoutubeMeta get(UUID recipeId) throws YoutubeMetaException {
        return repository.find(recipeId);
    }

    /**
     * 여러 레시피 ID로 유튜브 메타 정보 목록 조회
     *
     * @param recipeIds 레시피 ID 목록
     * @return 유튜브 메타 정보 목록
     */
    public List<RecipeYoutubeMeta> gets(List<UUID> recipeIds) {
        return repository.finds(recipeIds);
    }

    /**
     * 레시피 유튜브 메타 정보 차단 (BLOCKED)
     *
     * <p>외부 API를 통해 차단 여부를 확인한 후 차단 처리합니다.</p>
     *
     * @param recipeId 레시피 ID
     * @throws YoutubeMetaException 메타 정보를 찾을 수 없거나 차단되지 않은 비디오일 때
     */
    public void block(UUID recipeId) throws YoutubeMetaException {
        RecipeYoutubeMeta youtubeMeta = repository.find(recipeId);
        YoutubeUri youtubeUri = YoutubeUri.from(youtubeMeta.getVideoUri());
        if (client.isBlocked(youtubeUri)) {
            repository.block(recipeId);
        } else {
            throw new YoutubeMetaException(YoutubeMetaErrorCode.YOUTUBE_META_NOT_BLOCKED_VIDEO);
        }
    }

    private void validateAllActive(List<RecipeYoutubeMeta> metas) throws YoutubeMetaException {
        if (metas.stream().anyMatch(RecipeYoutubeMeta::isBanned)) {
            throw new YoutubeMetaException(YoutubeMetaErrorCode.YOUTUBE_META_BANNED);
        }
        if (metas.stream().anyMatch(RecipeYoutubeMeta::isBlocked)) {
            throw new YoutubeMetaException(YoutubeMetaErrorCode.YOUTUBE_META_BLOCKED);
        }
    }
}
