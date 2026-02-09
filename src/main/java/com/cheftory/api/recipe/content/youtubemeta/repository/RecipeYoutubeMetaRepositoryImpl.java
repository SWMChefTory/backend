package com.cheftory.api.recipe.content.youtubemeta.repository;

import com.cheftory.api.recipe.content.youtubemeta.entity.RecipeYoutubeMeta;
import com.cheftory.api.recipe.content.youtubemeta.exception.YoutubeMetaErrorCode;
import com.cheftory.api.recipe.content.youtubemeta.exception.YoutubeMetaException;
import java.net.URI;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

/**
 * 레시피 유튜브 메타 Repository 구현체
 */
@Repository
@RequiredArgsConstructor
public class RecipeYoutubeMetaRepositoryImpl implements RecipeYoutubeMetaRepository {

    private final RecipeYoutubeMetaJpaRepository repository;

    /**
     * 비디오 URI로 메타 정보 목록 조회
     *
     * @param videoUri 비디오 URI
     * @return 메타 정보 목록
     */
    @Override
    public List<RecipeYoutubeMeta> find(URI videoUri) {
        return repository.findAllByVideoUri(videoUri);
    }

    /**
     * 여러 레시피 ID로 메타 정보 목록 조회
     *
     * @param recipeIds 레시피 ID 목록
     * @return 메타 정보 목록
     */
    @Override
    public List<RecipeYoutubeMeta> finds(List<UUID> recipeIds) {
        return repository.findAllByRecipeIdIn(recipeIds);
    }

    /**
     * 레시피 ID로 메타 정보 조회
     *
     * @param recipeId 레시피 ID
     * @return 메타 정보
     * @throws YoutubeMetaException 메타 정보를 찾을 수 없을 때
     */
    @Override
    public RecipeYoutubeMeta find(UUID recipeId) throws YoutubeMetaException {
        return repository
                .findByRecipeId(recipeId)
                .orElseThrow(() -> new YoutubeMetaException(YoutubeMetaErrorCode.YOUTUBE_META_NOT_FOUND));
    }

    /**
     * 유튜브 메타 정보 생성
     *
     * @param recipeYoutubeMeta 저장할 메타 정보
     * @return 저장된 메타 정보
     */
    @Override
    public RecipeYoutubeMeta create(RecipeYoutubeMeta recipeYoutubeMeta) {
        return repository.save(recipeYoutubeMeta);
    }

    /**
     * 레시피 유튜브 메타 정보 차단 (BANNED)
     *
     * @param recipeId 레시피 ID
     * @throws YoutubeMetaException 메타 정보를 찾을 수 없을 때
     */
    @Override
    public void ban(UUID recipeId) throws YoutubeMetaException {
        RecipeYoutubeMeta youtubeMeta = repository
                .findByRecipeId(recipeId)
                .orElseThrow(() -> new YoutubeMetaException(YoutubeMetaErrorCode.YOUTUBE_META_NOT_FOUND));
        youtubeMeta.ban();
        repository.save(youtubeMeta);
    }

    /**
     * 레시피 유튜브 메타 정보 실패 처리
     *
     * @param recipeId 레시피 ID
     */
    @Override
    public void failed(UUID recipeId) {
        repository.findByRecipeId(recipeId).ifPresent(meta -> {
            meta.failed();
            repository.save(meta);
        });
    }

    /**
     * 레시피 유튜브 메타 정보 차단 (BLOCKED)
     *
     * @param recipeId 레시피 ID
     * @throws YoutubeMetaException 메타 정보를 찾을 수 없을 때
     */
    @Override
    public void block(UUID recipeId) throws YoutubeMetaException {
        RecipeYoutubeMeta youtubeMeta = repository
                .findByRecipeId(recipeId)
                .orElseThrow(() -> new YoutubeMetaException(YoutubeMetaErrorCode.YOUTUBE_META_NOT_FOUND));
        youtubeMeta.block();
        repository.save(youtubeMeta);
    }
}
