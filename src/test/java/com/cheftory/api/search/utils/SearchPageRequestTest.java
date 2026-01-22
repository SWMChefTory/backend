package com.cheftory.api.search.utils;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Pageable;

@DisplayName("SearchPageRequest")
class SearchPageRequestTest {

    @Test
    @DisplayName("should create pageable with default size")
    void shouldCreatePageableWithDefaultSize() {
        Pageable pageable = SearchPageRequest.create(2);

        assertThat(pageable.getPageNumber()).isEqualTo(2);
        assertThat(pageable.getPageSize()).isEqualTo(10);
    }
}
