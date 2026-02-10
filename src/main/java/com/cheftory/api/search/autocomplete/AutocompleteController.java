package com.cheftory.api.search.autocomplete;

import com.cheftory.api.search.exception.SearchException;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 자동완성 컨트롤러.
 *
 * <p>검색어 자동완성 기능을 제공하는 REST API 엔드포인트를 정의합니다.</p>
 */
@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class AutocompleteController {

    /** 자동완성 서비스. */
    private final AutocompleteService autocompleteService;

    /**
     * 자동완성 검색어를 조회합니다.
     *
     * @param query 검색어
     * @param scope 검색 범위 (기본값: RECIPE)
     * @return 자동완성 검색어 응답
     * @throws SearchException 검색 예외
     */
    @GetMapping("/search/autocomplete")
    public AutocompletesResponse getAutocomplete(
            @RequestParam("query") String query,
            @RequestParam(value = "scope", defaultValue = "RECIPE") AutocompleteScope scope)
            throws SearchException {
        List<Autocomplete> autocompletes = autocompleteService.autocomplete(scope, query);
        return AutocompletesResponse.from(autocompletes);
    }
}
