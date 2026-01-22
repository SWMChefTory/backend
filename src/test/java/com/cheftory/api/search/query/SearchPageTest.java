package com.cheftory.api.search.query;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("SearchPage")
class SearchPageTest {

    @Test
    @DisplayName("should expose items and cursor")
    void shouldExposeItemsAndCursor() {
        List<UUID> items = List.of(UUID.randomUUID(), UUID.randomUUID());
        SearchPage page = new SearchPage(items, "next-cursor");

        assertThat(page.items()).isEqualTo(items);
        assertThat(page.nextCursor()).isEqualTo("next-cursor");
    }
}
