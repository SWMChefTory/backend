package com.cheftory.api.recipe.content.youtubemeta.repository;

import com.cheftory.api.recipe.content.youtubemeta.entity.RecipeYoutubeMeta;
import com.cheftory.api.recipe.content.youtubemeta.exception.YoutubeMetaException;
import java.util.List;
import java.util.UUID;

/**
 * 레시피-YouTube 메타 도메인 저장소 인터페이스.
 *
 * <p>조회는 `recipeId`/`videoId` 기준을 모두 지원하고, 생성은 `recipeId` 유니크 기반 idempotent 처리를 전제로 합니다.</p>
 */
public interface RecipeYoutubeMetaRepository {
    /**
     * 비디오 ID로 메타 정보 목록 조회
     *
     * @param videoId 비디오 ID
     * @return 메타 정보 목록
     */
    List<RecipeYoutubeMeta> find(String videoId);

    /**
     * 여러 레시피 ID로 메타 정보 목록 조회
     *
     * @param recipeIds 레시피 ID 목록
     * @return 메타 정보 목록
     */
    List<RecipeYoutubeMeta> finds(List<UUID> recipeIds);

    /**
     * 레시피 ID로 메타 정보 조회
     *
     * @param recipeId 레시피 ID
     * @return 메타 정보
     * @throws YoutubeMetaException 메타 정보를 찾을 수 없을 때
     */
    RecipeYoutubeMeta find(UUID recipeId) throws YoutubeMetaException;

    /**
     * 레시피 ID로 메타 정보 존재 여부 확인
     *
     * @param recipeId 레시피 ID
     * @return 존재 여부
     */
    boolean exists(UUID recipeId);

    /**
     * 유튜브 메타 정보 생성
     *
     * @param recipeYoutubeMeta 저장할 메타 정보
     *
     * <p>구현체는 `recipeId` 유니크 충돌을 재시도-safe하게 처리할 수 있습니다.</p>
     */
    void create(RecipeYoutubeMeta recipeYoutubeMeta);
}
