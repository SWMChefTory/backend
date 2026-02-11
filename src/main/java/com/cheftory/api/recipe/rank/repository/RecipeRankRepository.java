package com.cheftory.api.recipe.rank.repository;

import com.cheftory.api._common.cursor.CursorException;
import com.cheftory.api._common.cursor.CursorPage;
import com.cheftory.api.recipe.rank.RankingType;
import com.cheftory.api.recipe.rank.exception.RecipeRankException;
import java.time.Duration;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

/**
 * 레시피 랭킹 Repository 인터페이스.
 *
 * <p>Redis를 사용하여 레시피 랭킹 정보를 관리합니다.</p>
 */
public interface RecipeRankRepository {
    /**
     * 랭킹 정보 저장.
     *
     * @param key 키
     * @param recipeId 레시피 ID
     * @param rank 순위
     */
    void saveRanking(String key, UUID recipeId, Integer rank);

    /**
     * 키 만료 시간 설정.
     *
     * @param key 키
     * @param duration 만료 기간
     */
    void setExpire(String key, Duration duration);

    /**
     * 최신 랭킹 키 포인터 저장.
     *
     * @param pointerKey 포인터 키
     * @param realKey 실제 키
     */
    void saveLatest(String pointerKey, String realKey);

    /**
     * 최신 랭킹 키 조회.
     *
     * @param pointerKey 포인터 키
     * @return 실제 키
     */
    Optional<String> findLatest(String pointerKey);

    /**
     * 레시피 ID 목록 조회 (범위).
     *
     * @param key 키
     * @param start 시작 인덱스
     * @param end 종료 인덱스
     * @return 레시피 ID 목록
     */
    Set<String> findRecipeIds(String key, long start, long end);

    /**
     * 랭킹 항목 개수 조회.
     *
     * @param key 키
     * @return 항목 개수
     */
    Long count(String key);

    /**
     * 순위별 레시피 ID 목록 조회.
     *
     * @param key 키
     * @param startRank 시작 순위
     * @param count 조회 개수
     * @return 레시피 ID 목록
     */
    List<String> findRecipeIdsByRank(String key, int startRank, int count);

    /**
     * 랭킹 타입별 레시피 ID 목록을 커서 기반 페이징으로 조회합니다 (첫 페이지).
     *
     * @param type 랭킹 타입
     * @return 페이징된 레시피 ID 목록
     * @throws RecipeRankException 랭킹 정보를 찾을 수 없는 경우
     */
    CursorPage<UUID> getRecipeIdsFirst(RankingType type) throws RecipeRankException;

    /**
     * 랭킹 타입별 레시피 ID 목록을 커서 기반 페이징으로 조회합니다.
     *
     * @param type 랭킹 타입
     * @param cursor 페이징 커서
     * @return 페이징된 레시피 ID 목록
     * @throws CursorException 유효하지 않은 커서일 때
     */
    CursorPage<UUID> getRecipeIds(RankingType type, String cursor) throws CursorException;
}
