package com.cheftory.api._common.cursor;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("RankingCursorCodec")
class RankingCursorCodecTest {

    @Test
    @DisplayName("should encode and decode cursor")
    void shouldEncodeAndDecodeCursor() {
        RankingCursorCodec codec = new RankingCursorCodec();
        RankingCursor cursor = new RankingCursor(UUID.randomUUID(), "after");

        String encoded = codec.encode(cursor);
        RankingCursor decoded = codec.decode(encoded);

        assertThat(decoded.requestId()).isEqualTo(cursor.requestId());
        assertThat(decoded.searchAfter()).isEqualTo(cursor.searchAfter());
    }
}
