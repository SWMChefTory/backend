package com.cheftory.api.recipe.content.youtubemeta.repository;

import com.cheftory.api.recipe.content.youtubemeta.entity.RecipeYoutubeMeta;
import com.cheftory.api.recipe.content.youtubemeta.exception.YoutubeMetaErrorCode;
import com.cheftory.api.recipe.content.youtubemeta.exception.YoutubeMetaException;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Repository;

/**
 * 레시피 유튜브 메타 Repository 구현체.
 *
 * <p>`recipeId` 유니크 제약을 활용해 메타 생성 중복 충돌을 허용 가능한(idempotent) 동작으로 처리합니다.</p>
 */
@Repository
@RequiredArgsConstructor
public class RecipeYoutubeMetaRepositoryImpl implements RecipeYoutubeMetaRepository {

    private final RecipeYoutubeMetaJpaRepository repository;

    /**
     * 비디오 ID로 메타 정보 목록 조회
     *
     * @param videoId 비디오 ID
     * @return 메타 정보 목록
     */
    @Override
    public List<RecipeYoutubeMeta> find(String videoId) {
        return repository.findAllByVideoId(videoId);
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

    @Override
    public boolean exists(UUID recipeId) {
        return repository.existsByRecipeId(recipeId);
    }

    /**
     * 유튜브 메타 정보 생성
     *
     * @param recipeYoutubeMeta 저장할 메타 정보
     *
     * <p>`recipe_id` 유니크 충돌 시(동시 생성/재시도 경쟁)에는 예외를 삼키고 기존 row를 재사용 가능한 상태로 둡니다.</p>
     */
    @Override
    public void create(RecipeYoutubeMeta recipeYoutubeMeta) {
        try {
            repository.save(recipeYoutubeMeta);
        } catch (DataIntegrityViolationException ignored) {
            // 동시성 경쟁에서 이미 같은 recipeId 메타가 저장된 경우를 허용한다.
        }
    }
}
