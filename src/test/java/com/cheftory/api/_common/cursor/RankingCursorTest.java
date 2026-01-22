package com.cheftory.api._common.cursor;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("RankingCursor")
class RankingCursorTest {

    @Test
    @DisplayName("should expose requestId and searchAfter")
    void shouldExposeFields() {
        UUID requestId = UUID.randomUUID();
        RankingCursor cursor = new RankingCursor(requestId, "after");

        assertThat(cursor.requestId()).isEqualTo(requestId);
        assertThat(cursor.searchAfter()).isEqualTo("after");
    }
}
