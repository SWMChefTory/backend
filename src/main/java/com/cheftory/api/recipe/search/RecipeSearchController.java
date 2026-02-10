package com.cheftory.api.recipe.search;

import com.cheftory.api._common.security.UserPrincipal;
import com.cheftory.api.search.exception.SearchException;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 레시피 검색 관련 API 요청을 처리하는 컨트롤러.
 *
 * <p>레시피 검색 기능을 제공합니다.</p>
 */
@RestController
@RequiredArgsConstructor
@RequestMapping
public class RecipeSearchController {

    private final RecipeSearchFacade recipeSearchFacade;

    /**
     * 레시피를 검색합니다.
     *
     * @param query 검색어
     * @param cursor 커서
     * @param userId 사용자 ID
     * @return 검색된 레시피 목록 응답
     * @throws SearchException 검색 실패 시
     */
    @GetMapping("/api/v1/recipes/search")
    public SearchedRecipesResponse searchRecipes(
            @RequestParam("query") String query,
            @RequestParam(required = false) String cursor,
            @UserPrincipal UUID userId)
            throws SearchException {
        return SearchedRecipesResponse.from(recipeSearchFacade.searchRecipes(query, userId, cursor));
    }
}
