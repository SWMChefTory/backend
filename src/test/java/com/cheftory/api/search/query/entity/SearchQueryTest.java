package com.cheftory.api.search.query.entity;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("SearchQuery Entity")
class SearchQueryTest {

    @Test
    @DisplayName("builder should set fields")
    void builderShouldSetFields() {
        List<String> keywords = List.of("korean", "stew");

        SearchQuery query = SearchQuery.builder()
                .id("id-1")
                .searchText("kimchi stew")
                .channelTitle("channel-name")
                .keywords(keywords)
                .build();

        assertThat(query.getId()).isEqualTo("id-1");
        assertThat(query.getSearchText()).isEqualTo("kimchi stew");
        assertThat(query.getChannelTitle()).isEqualTo("channel-name");
        assertThat(query.getKeywords()).isEqualTo(keywords);
    }
}
