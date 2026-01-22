package com.cheftory.api.search.query;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("SearchQueryScope")
class SearchQueryScopeTest {

    @Test
    @DisplayName("should include RECIPE scope")
    void shouldIncludeRecipeScope() {
        assertThat(SearchQueryScope.valueOf("RECIPE")).isEqualTo(SearchQueryScope.RECIPE);
        assertThat(SearchQueryScope.values()).containsExactly(SearchQueryScope.RECIPE);
    }
}
