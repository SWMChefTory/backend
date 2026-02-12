package com.cheftory.api.search;

import com.cheftory.api._common.cursor.CursorPage;
import com.cheftory.api.recipe.search.RecipeSearchPort;
import com.cheftory.api.recipe.search.exception.RecipeSearchErrorCode;
import com.cheftory.api.recipe.search.exception.RecipeSearchException;
import com.cheftory.api.search.exception.SearchException;
import com.cheftory.api.search.query.SearchQueryScope;
import com.cheftory.api.search.query.entity.SearchQuery;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * 레시피 검색 어댑터.
 *
 * <p>검색 파사드를 사용하여 레시피 검색 포트를 구현합니다.</p>
 */
@Service
@RequiredArgsConstructor
public class RecipeSearchAdapter implements RecipeSearchPort {

    /** 검색 파사드. */
    private final SearchFacade searchFacade;

    /**
     * 레시피 ID 목록을 검색합니다.
     *
     * @param userId 사용자 ID
     * @param query 검색어
     * @param cursor 커서
     * @return 레시피 ID 커서 페이지
     * @throws RecipeSearchException 처리 예외
     */
    @Override
    public CursorPage<UUID> searchRecipeIds(UUID userId, String query, String cursor) throws RecipeSearchException {
        try {
            CursorPage<SearchQuery> results = searchFacade.search(SearchQueryScope.RECIPE, userId, query, cursor);
            List<UUID> items = results.items().stream()
                    .map(s -> UUID.fromString(s.getId()))
                    .toList();
            return CursorPage.of(items, results.nextCursor());
        } catch (SearchException exception) {
            throw new RecipeSearchException(RecipeSearchErrorCode.RECIPE_SEARCH_FAILED);
        }
    }
}
