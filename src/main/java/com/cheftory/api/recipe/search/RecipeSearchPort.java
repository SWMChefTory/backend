package com.cheftory.api.recipe.search;

import com.cheftory.api._common.cursor.CursorPage;
import com.cheftory.api.recipe.search.exception.RecipeSearchException;
import java.util.UUID;

/**
 * 레시피 검색 포트.
 *
 * <p>OpenSearch를 사용하여 레시피 검색을 수행합니다.</p>
 */
public interface RecipeSearchPort {
    /**
     * 레시피 ID 목록 검색.
     *
     * @param userId 사용자 ID
     * @param query 검색어
     * @param cursor 페이징 커서
     * @return 레시피 ID 커서 페이지
     * @throws RecipeSearchException 검색 실패 시
     */
    CursorPage<UUID> searchRecipeIds(UUID userId, String query, String cursor) throws RecipeSearchException;
}
