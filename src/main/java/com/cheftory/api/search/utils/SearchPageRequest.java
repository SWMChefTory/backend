package com.cheftory.api.search.utils;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

public class SearchPageRequest {
    private static final int DEFAULT_PAGE_SIZE = 10;

    public static Pageable create(int page) {
        return PageRequest.of(page, DEFAULT_PAGE_SIZE);
    }
}
