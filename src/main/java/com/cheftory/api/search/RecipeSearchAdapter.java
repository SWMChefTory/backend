package com.cheftory.api.search;

import com.cheftory.api._common.cursor.CursorPage;
import com.cheftory.api.recipe.search.RecipeSearchPort;
import com.cheftory.api.search.exception.SearchException;
import com.cheftory.api.search.query.SearchQueryScope;
import com.cheftory.api.search.query.entity.SearchQuery;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RecipeSearchAdapter implements RecipeSearchPort {
    private final SearchFacade searchFacade;

    @Override
    public CursorPage<UUID> searchRecipeIds(UUID userId, String query, String cursor) throws SearchException {
        CursorPage<SearchQuery> results = searchFacade.search(SearchQueryScope.RECIPE, userId, query, cursor);
        List<UUID> items =
                results.items().stream().map(s -> UUID.fromString(s.getId())).toList();
        return CursorPage.of(items, results.nextCursor());
    }
}
