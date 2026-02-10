package com.cheftory.api.search.autocomplete;

import com.cheftory.api.search.exception.SearchException;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 자동완성 서비스.
 *
 * <p>검색어 자동완성 기능을 제공합니다.</p>
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class AutocompleteService {

    /** 기본 자동완성 결과 최대 개수. */
    private static final int DEFAULT_LIMIT = 10;

    /** 자동완성 OpenSearch 리포지토리. */
    private final AutocompleteRepository autocompleteRepository;

    /**
     * 자동완성 검색을 수행합니다.
     *
     * @param scope 검색 범위
     * @param keyword 검색어
     * @return 자동완성 목록
     * @throws SearchException 검색 예외
     */
    public List<Autocomplete> autocomplete(AutocompleteScope scope, String keyword) throws SearchException {
        return autocompleteRepository.searchAutocomplete(scope, keyword, DEFAULT_LIMIT);
    }
}
