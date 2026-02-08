package com.cheftory.api.recipe.bookmark;

import com.cheftory.api._common.Clock;
import com.cheftory.api._common.cursor.CursorException;
import com.cheftory.api._common.cursor.CursorPage;
import com.cheftory.api.recipe.bookmark.entity.RecipeBookmark;
import com.cheftory.api.recipe.bookmark.entity.RecipeBookmarkCategorizedCount;
import com.cheftory.api.recipe.bookmark.entity.RecipeBookmarkCategorizedCountProjection;
import com.cheftory.api.recipe.bookmark.entity.RecipeBookmarkUnCategorizedCount;
import com.cheftory.api.recipe.bookmark.entity.RecipeBookmarkUnCategorizedCountProjection;
import com.cheftory.api.recipe.bookmark.exception.RecipeBookmarkException;
import com.cheftory.api.recipe.bookmark.repository.RecipeBookmarkRepository;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * 레시피 북마크 도메인의 비즈니스 로직을 처리하는 서비스
 */
@Service
@RequiredArgsConstructor
public class RecipeBookmarkService {
    private final RecipeBookmarkRepository recipeBookmarkRepository;
    private final Clock clock;

    /**
     * 레시피 북마크 생성
     *
     * @param userId 사용자 ID
     * @param recipeId 레시피 ID
     * @return 생성 성공 여부
     * @throws RecipeBookmarkException 이미 북마크가 존재하는 경우
     */
    public boolean create(UUID userId, UUID recipeId) throws RecipeBookmarkException {
        try {
            recipeBookmarkRepository.create(RecipeBookmark.create(clock, userId, recipeId));
            return true;
        } catch (RecipeBookmarkException e) {
            return recipeBookmarkRepository.recreate(userId, recipeId, clock);
        }
    }

    /**
     * 레시피 북마크 존재 여부 확인
     *
     * @param userId 사용자 ID
     * @param recipeId 레시피 ID
     * @return 북마크 존재 여부
     */
    public boolean exist(UUID userId, UUID recipeId) {
        return recipeBookmarkRepository.exists(userId, recipeId);
    }

    /**
     * 레시피 북마크 조회
     *
     * @param userId 사용자 ID
     * @param recipeId 레시피 ID
     * @return 조회된 북마크
     * @throws RecipeBookmarkException 북마크를 찾을 수 없을 때
     */
    public RecipeBookmark get(UUID userId, UUID recipeId) throws RecipeBookmarkException {
        return recipeBookmarkRepository.get(userId, recipeId);
    }

    /**
     * 레시피 북마크 카테고리 설정
     *
     * @param userId 사용자 ID
     * @param recipeId 레시피 ID
     * @param categoryId 카테고리 ID
     * @throws RecipeBookmarkException 북마크를 찾을 수 없을 때
     */
    public void categorize(UUID userId, UUID recipeId, UUID categoryId) throws RecipeBookmarkException {
        recipeBookmarkRepository.categorize(userId, recipeId, categoryId);
    }

    /**
     * 카테고리의 모든 레시피 북마크 카테고리 해제
     *
     * @param categoryId 카테고리 ID
     * @throws RecipeBookmarkException 낙관적 락 실패 시
     */
    public void unCategorize(UUID categoryId) throws RecipeBookmarkException {
        recipeBookmarkRepository.unCategorize(categoryId);
    }

    /**
     * 레시피 북마크 삭제
     *
     * @param userId 사용자 ID
     * @param recipeId 레시피 ID
     * @throws RecipeBookmarkException 북마크를 찾을 수 없을 때
     */
    public void delete(UUID userId, UUID recipeId) throws RecipeBookmarkException {
        recipeBookmarkRepository.delete(userId, recipeId, clock);
    }

    /**
     * 레시피 북마크 일괄 삭제
     *
     * @param recipeBookmarkIds 북마크 ID 목록
     */
    public void deletes(List<UUID> recipeBookmarkIds) {
        recipeBookmarkRepository.deletes(recipeBookmarkIds, clock);
    }

    /**
     * 레시피 ID로 레시피 북마크 목록 조회
     *
     * @param recipeId 레시피 ID
     * @return 북마크 목록
     */
    public List<RecipeBookmark> gets(UUID recipeId) {
        return recipeBookmarkRepository.gets(recipeId);
    }

    /**
     * 레시피 ID로 관련 모든 레시피 북마크 차단
     *
     * @param recipeId 레시피 ID
     */
    public void block(UUID recipeId) {
        recipeBookmarkRepository.block(recipeId, clock);
    }

    /**
     * 카테고리별 레시피 북마크 목록 조회 (커서 기반 페이징)
     *
     * @param userId 사용자 ID
     * @param categoryId 카테고리 ID
     * @param cursor 페이징 커서 (null이면 첫 페이지)
     * @return 페이징된 북마크 목록
     * @throws CursorException 유효하지 않은 커서일 때
     */
    public CursorPage<RecipeBookmark> getCategorized(UUID userId, UUID categoryId, String cursor)
            throws CursorException {
        boolean first = (cursor == null || cursor.isBlank());

        return first
                ? recipeBookmarkRepository.keysetCategorizedFirst(userId, categoryId)
                : recipeBookmarkRepository.keysetCategorized(userId, categoryId, cursor);
    }

    /**
     * 최근 레시피 북마크 목록 조회 (커서 기반 페이징)
     *
     * @param userId 사용자 ID
     * @param cursor 페이징 커서 (null이면 첫 페이지)
     * @return 페이징된 북마크 목록
     * @throws CursorException 유효하지 않은 커서일 때
     */
    public CursorPage<RecipeBookmark> getRecents(UUID userId, String cursor) throws CursorException {
        boolean first = (cursor == null || cursor.isBlank());

        return first
                ? recipeBookmarkRepository.keysetRecentsFirst(userId)
                : recipeBookmarkRepository.keysetRecents(userId, cursor);
    }

    /**
     * 카테고리별 레시피 북마크 개수 조회
     *
     * @param categoryIds 카테고리 ID 목록
     * @return 카테고리별 북마크 개수 목록
     */
    public List<RecipeBookmarkCategorizedCount> countByCategories(List<UUID> categoryIds) {
        List<RecipeBookmarkCategorizedCountProjection> projections =
                recipeBookmarkRepository.countCategorized(categoryIds);
        return projections.stream()
                .map(projection -> RecipeBookmarkCategorizedCount.of(
                        projection.getCategoryId(), projection.getCount().intValue()))
                .toList();
    }

    /**
     * 카테고리 없는 레시피 북마크 개수 조회
     *
     * @param userId 사용자 ID
     * @return 카테고리 없는 북마크 개수
     */
    public RecipeBookmarkUnCategorizedCount countUncategorized(UUID userId) {
        RecipeBookmarkUnCategorizedCountProjection projection = recipeBookmarkRepository.countUncategorized(userId);
        return RecipeBookmarkUnCategorizedCount.of(projection.getCount().intValue());
    }

    /**
     * 레시피 ID 목록으로 사용자의 레시피 북마크 목록 조회
     *
     * @param recipeIds 레시피 ID 목록
     * @param userId 사용자 ID
     * @return 북마크 목록
     */
    public List<RecipeBookmark> gets(List<UUID> recipeIds, UUID userId) {
        return recipeBookmarkRepository.gets(userId, recipeIds);
    }
}
