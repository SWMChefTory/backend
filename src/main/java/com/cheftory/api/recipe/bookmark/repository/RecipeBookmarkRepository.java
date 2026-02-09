package com.cheftory.api.recipe.bookmark.repository;

import com.cheftory.api._common.Clock;
import com.cheftory.api._common.cursor.CursorException;
import com.cheftory.api._common.cursor.CursorPage;
import com.cheftory.api.recipe.bookmark.entity.RecipeBookmark;
import com.cheftory.api.recipe.bookmark.entity.RecipeBookmarkCategorizedCountProjection;
import com.cheftory.api.recipe.bookmark.entity.RecipeBookmarkUnCategorizedCountProjection;
import com.cheftory.api.recipe.bookmark.exception.RecipeBookmarkException;
import java.util.List;
import java.util.UUID;

/**
 * 레시피 북마크 데이터 접근 레이어
 */
public interface RecipeBookmarkRepository {
    /**
     * 레시피 북마크 생성
     *
     * @param recipeBookmark 생성할 북마크 엔티티
     * @return 생성 성공 여부
     * @throws RecipeBookmarkException 이미 존재하는 북마크일 때
     */
    boolean create(RecipeBookmark recipeBookmark) throws RecipeBookmarkException;

    /**
     * 삭제된 레시피 북마크 재생성
     *
     * @param userId 사용자 ID
     * @param recipeId 레시피 ID
     * @param clock 시계
     * @return 재생성 성공 여부
     * @throws RecipeBookmarkException 북마크를 찾을 수 없거나 낙관적 락 실패 시
     */
    boolean recreate(UUID userId, UUID recipeId, Clock clock) throws RecipeBookmarkException;

    /**
     * 레시피 북마크 존재 여부 확인
     *
     * @param userId 사용자 ID
     * @param recipeId 레시피 ID
     * @return 북마크 존재 여부
     */
    boolean exists(UUID userId, UUID recipeId);

    /**
     * 레시피 북마크 카테고리 설정
     *
     * @param userId 사용자 ID
     * @param recipeId 레시피 ID
     * @param categoryId 카테고리 ID
     * @throws RecipeBookmarkException 북마크를 찾을 수 없거나 낙관적 락 실패 시
     */
    void categorize(UUID userId, UUID recipeId, UUID categoryId) throws RecipeBookmarkException;

    /**
     * 카테고리의 모든 레시피 북마크 카테고리 해제
     *
     * @param categoryId 카테고리 ID
     * @throws RecipeBookmarkException 낙관적 락 실패 시
     */
    void unCategorize(UUID categoryId) throws RecipeBookmarkException;

    /**
     * 레시피 북마크 삭제
     *
     * @param userId 사용자 ID
     * @param recipeId 레시피 ID
     * @param clock 시계
     * @throws RecipeBookmarkException 북마크를 찾을 수 없을 때
     */
    void delete(UUID userId, UUID recipeId, Clock clock) throws RecipeBookmarkException;

    /**
     * 레시피 북마크 일괄 삭제
     *
     * @param recipeBookmarkIds 북마크 ID 목록
     * @param clock 시계
     */
    void deletes(List<UUID> recipeBookmarkIds, Clock clock);

    /**
     * 레시피 ID로 관련 모든 레시피 북마크 차단
     *
     * @param recipeId 레시피 ID
     * @param clock 시계
     */
    void block(UUID recipeId, Clock clock);

    /**
     * 레시피 북마크 조회
     *
     * @param userId 사용자 ID
     * @param recipeId 레시피 ID
     * @return 조회된 북마크
     * @throws RecipeBookmarkException 북마크를 찾을 수 없을 때
     */
    RecipeBookmark get(UUID userId, UUID recipeId) throws RecipeBookmarkException;

    /**
     * 최근 레시피 북마크 목록 조회 (커서 기반 페이징)
     *
     * @param userId 사용자 ID
     * @param cursor 페이징 커서
     * @return 페이징된 북마크 목록
     * @throws CursorException 유효하지 않은 커서일 때
     */
    CursorPage<RecipeBookmark> keysetRecents(UUID userId, String cursor) throws CursorException;

    /**
     * 최근 레시피 북마크 첫 페이지 조회
     *
     * @param userId 사용자 ID
     * @return 페이징된 북마크 목록 (첫 페이지)
     */
    CursorPage<RecipeBookmark> keysetRecentsFirst(UUID userId) throws CursorException;

    /**
     * 카테고리별 레시피 북마크 목록 조회 (커서 기반 페이징)
     *
     * @param userId 사용자 ID
     * @param categoryId 카테고리 ID
     * @param cursor 페이징 커서
     * @return 페이징된 북마크 목록
     * @throws CursorException 유효하지 않은 커서일 때
     */
    CursorPage<RecipeBookmark> keysetCategorized(UUID userId, UUID categoryId, String cursor) throws CursorException;

    /**
     * 카테고리별 레시피 북마크 첫 페이지 조회
     *
     * @param userId 사용자 ID
     * @param categoryId 카테고리 ID
     * @return 페이징된 북마크 목록 (첫 페이지)
     */
    CursorPage<RecipeBookmark> keysetCategorizedFirst(UUID userId, UUID categoryId);

    /**
     * 카테고리별 레시피 북마크 개수 조회
     *
     * @param categoryIds 카테고리 ID 목록
     * @return 카테고리별 북마크 개수 목록
     */
    List<RecipeBookmarkCategorizedCountProjection> countCategorized(List<UUID> categoryIds);

    /**
     * 카테고리 없는 레시피 북마크 개수 조회
     *
     * @param userId 사용자 ID
     * @return 카테고리 없는 북마크 개수
     */
    RecipeBookmarkUnCategorizedCountProjection countUncategorized(UUID userId);

    /**
     * 레시피 ID 목록으로 사용자의 레시피 북마크 목록 조회
     *
     * @param userId 사용자 ID
     * @param recipeIds 레시피 ID 목록
     * @return 북마크 목록
     */
    List<RecipeBookmark> gets(UUID userId, List<UUID> recipeIds);

    /**
     * 레시피 ID로 레시피 북마크 목록 조회
     *
     * @param recipeId 레시피 ID
     * @return 북마크 목록
     */
    List<RecipeBookmark> gets(UUID recipeId);
}
