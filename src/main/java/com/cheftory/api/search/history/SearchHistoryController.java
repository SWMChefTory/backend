package com.cheftory.api.search.history;

import com.cheftory.api._common.reponse.SuccessOnlyResponse;
import com.cheftory.api._common.security.UserPrincipal;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 검색 히스토리 컨트롤러.
 *
 * <p>사용자의 검색 히스토리를 관리하는 REST API 엔드포인트를 정의합니다.</p>
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1")
public class SearchHistoryController {

    /** 검색 히스토리 서비스. */
    private final SearchHistoryService searchHistoryService;

    /**
     * 검색 히스토리를 조회합니다.
     *
     * @param userId 사용자 ID
     * @param scope 검색 범위 (기본값: RECIPE)
     * @return 검색 히스토리 응답
     */
    @GetMapping("/search/histories")
    public SearchHistoriesResponse getSearchHistories(
            @UserPrincipal UUID userId,
            @RequestParam(value = "scope", defaultValue = "RECIPE") SearchHistoryScope scope) {
        return SearchHistoriesResponse.from(searchHistoryService.get(userId, scope));
    }

    /**
     * 특정 검색 히스토리를 삭제합니다.
     *
     * @param userId 사용자 ID
     * @param scope 검색 범위 (기본값: RECIPE)
     * @param text 삭제할 검색어
     * @return 성공 응답
     */
    @DeleteMapping(value = "/search/histories", params = "text")
    public SuccessOnlyResponse deleteSearchHistory(
            @UserPrincipal UUID userId,
            @RequestParam(value = "scope", defaultValue = "RECIPE") SearchHistoryScope scope,
            @RequestParam("text") String text) {
        searchHistoryService.delete(userId, scope, text);
        return SuccessOnlyResponse.create();
    }

    /**
     * 모든 검색 히스토리를 삭제합니다.
     *
     * @param userId 사용자 ID
     * @param scope 검색 범위 (기본값: RECIPE)
     * @return 성공 응답
     */
    @DeleteMapping("/search/histories")
    public SuccessOnlyResponse deleteAllSearchHistories(
            @UserPrincipal UUID userId,
            @RequestParam(value = "scope", defaultValue = "RECIPE") SearchHistoryScope scope) {
        searchHistoryService.deleteAll(userId, scope);
        return SuccessOnlyResponse.create();
    }
}
