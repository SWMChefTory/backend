package com.cheftory.api.recipe.content.youtubemeta.repository;

import com.cheftory.api.recipe.content.youtubemeta.entity.RecipeYoutubeMeta;
import com.cheftory.api.recipe.content.youtubemeta.exception.YoutubeMetaException;
import java.net.URI;
import java.util.List;
import java.util.UUID;

/**
 * 레시피 유튜브 메타 Repository 인터페이스
 */
public interface RecipeYoutubeMetaRepository {
    /**
     * 비디오 URI로 메타 정보 목록 조회
     *
     * @param videoUri 비디오 URI
     * @return 메타 정보 목록
     */
    List<RecipeYoutubeMeta> find(URI videoUri);

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
     * 유튜브 메타 정보 생성
     *
     * @param recipeYoutubeMeta 저장할 메타 정보
     * @return 저장된 메타 정보
     */
    RecipeYoutubeMeta create(RecipeYoutubeMeta recipeYoutubeMeta);

    /**
     * 레시피 유튜브 메타 정보 차단 (BANNED)
     *
     * @param recipeId 레시피 ID
     * @throws YoutubeMetaException 메타 정보를 찾을 수 없을 때
     */
    void ban(UUID recipeId) throws YoutubeMetaException;

    /**
     * 레시피 유튜브 메타 정보 실패 처리
     *
     * @param recipeId 레시피 ID
     */
    void failed(UUID recipeId);

    void block(UUID recipeId) throws YoutubeMetaException;
}
